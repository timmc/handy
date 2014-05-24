(ns org.timmc.t-handy
  (:use clojure.test
        org.timmc.handy))

;;;; testing

(defn sign
  "Compute the signum of the number as an integer. (-1, 0, or 1)"
  [v]
  (-> v double Math/signum int))

(deftest selftest
  (are [i o] (= (sign i) o)
       0 0
       0.0 0
       1/3 1
       -1e3 -1
       5 1
       -6 -1
       5M 1
       (java.math.BigInteger/valueOf -2) -1))

;;;; Control flow

(deftest multi-clause
  (testing "no execution of else-expr if then-expr is invoked"
    (let [mutation (atom 0)]
      (is (= (if-let+ [[a b] (range 5 10)
                       c (+ a b)]
                      c
                      (swap! mutation inc))
             11))
      (is (zero? @mutation))))
  (testing "no execution of then-expr if else-expr is invoked"
    (let [mutation (atom 0)]
      (is (= (if-let+ [[a b] (range 5 10)
                       c (= a b)]
                      (swap! mutation inc)
                      :else-clause)
             :else-clause))
      (is (zero? @mutation))))
  (testing "short-circuiting of bindings"
    (let [mutation (atom [])]
      (is (= (if-let+ [[a b] (range 5 10)
                       c (= a b)
                       d (swap! mutation conj :d)]
                      (swap! mutation conj :then)
                      (do (swap! mutation conj :else)
                          :else-clause))
             :else-clause))
      (is (= @mutation [:else]))))
  (testing "degenerate case"
    (is (= (if-let+ [] 4 5) 4)))
  (testing "no throwing things away"
    (is (thrown-with-msg? Exception #"bindings.*even"
          (macroexpand-1 '(if-let+ [one] 2 3))))))

;;;; Comparisons

(deftest lexicographic
  (are [main other out] (= (sign (lexicomp main other))
                           (sign out))
       [] nil 0 ;; equality of nil and []
       nil [1 2] -1 ;; nil is low
       [1 2] [] 1 ;; so is []
       [1 0] [1] 1 ;; 0 beats end
       [1 2] [1 2] 0 ;; general equality
       [9] [1 1 1] 1 ;; values beat length (cf. clojure.core/compare)
       [1] [9 9 9] -1 ;; unless they agree, of course!
       (range 5) [0 1 2 3] 1 ;; seqs against vectors
       () [] 0) ;; and lists
  ;; no non-sequential types
  (is (thrown? AssertionError (lexicomp {} [])))
  (is (thrown? AssertionError (lexicomp #{} []))))

(deftest versions
  (is (sequential? (version-norm "5.6")))
  (are [a e] (= (version-norm a) e)
       "0" []
       "0.3.0.5.0.0" [0 3 0 5]
       "67.1.12" [67 1 12]) ;; no splitting of multi-digit segments
  (are [vs b] (= (boolean (apply version<= vs)) b)
       ["1"] true ;; single element always true
       ["0" "1" "1" "2"] true ;; need not be strictly increasing
       ["1" "5" "0"] false ;; basic falsehood check
       ["0.2" "0.2.0.0"] true ;; equality with trailing-zero stripping...
       ["0.2.0.0" "0.2"] true ;; ...both ways
       ["0.0.9" "0.1"] true ;; no zero-stripping on front
       ["1.7" "18"] true)) ;; no splitting of multi-digit segments

;;;; Reflection

(defn out-of-order
  "Example function for tests."
  ([] 0)
  ([a b & c] :abc) ;; Intentionally *before* binary case to test minimality
  ([a b] :ab))

(defn only-n-ary
  [& c]
  :n)

(deftest arity-matching
  (testing "out-of-order example function"
    (is (= (matching-arity #'out-of-order 0) []))
    (is (= (matching-arity #'out-of-order 1) nil))
    (testing "Prefer binary case to n-ary case"
      (is (= (matching-arity #'out-of-order 2) '[a b])))
    (is (= (matching-arity #'out-of-order 3) '[a b & c]))
    (is (= (matching-arity #'out-of-order 500) '[a b & c])))
  (testing "only-n-ary example function"
    (is (= (matching-arity #'only-n-ary 0) '[& c]))
    (is (= (matching-arity #'only-n-ary 1) '[& c]))
    (is (= (matching-arity #'only-n-ary 500) '[& c])))
  (testing "can use bare arglist coll"
    (is (= (matching-arity '[[] [a b & c] [a b]] 2)
           '[a b]))
    (testing "including if reversed"
      (is (= (matching-arity '[[] [a b] [a b & c]] 2)
             '[a b]))))
  (testing "can use var meta"
    (is (= (matching-arity {:arglists '[[] [a b & c] [a b]]} 2)
           '[a b]))))

;;;; Sandboxing

(def test-a 8)

(deftest no-escape-or-capture
  (is (nil? (resolve 'join)))
  (is (nil? (resolve 'Color)))
  (is (= test-a 8)) ;; TODO: Why does resolve fail here?
  (is (= (with-temp-ns [(use '[clojure.string :only (join)])
                        (import 'java.awt.Color)
                        (def test-inner-b 12)]
           (if (resolve 'with-temp-ns)
             (throw (AssertionError. "Oops, captured outer 'use")))
           (if (resolve 'test-a)
             (throw (AssertionError. "Should not have resolved #'test-a")))
           [test-inner-b, (class Color), (join \, (range 5))])
         [12, Class, "0,1,2,3,4"]))
  (is (nil? (resolve 'test-inner-b)))
  (is (nil? (resolve 'join)))
  (is (nil? (resolve 'Color)))
  (is (= 8 (deref (resolve 'test-a)))))

(deftest get-errors
  ;; error in body
  (is (thrown-with-msg? Throwable #"testing-body" ;; might be wrapped exception
        (with-temp-ns [] (throw (RuntimeException. "testing-body")))))
  ;; error in ns
  (is (thrown-with-msg? Throwable #"testing-ns"
        (with-temp-ns [(throw (RuntimeException. "testing-ns"))])))
  ;; even compilation errors
  (is (thrown? clojure.lang.Compiler$CompilerException
               (with-temp-ns [(no-such 5 6 7)] (+ 4 5)))))

(deftest runtime-sequential-ns
  (is (= (with-temp-ns [(import 'java.awt.Point)
                        (def a (.getCanonicalName Point))]
           a)
         "java.awt.Point")))

(defmacro resolver
  [name-sym]
  (when-not (resolve name-sym)
    (throw (RuntimeException. "Could not resolve"))))

(deftest compile-sequential-ns
  (is (= (with-temp-ns [(import 'java.awt.Shape)
                        (require 'org.timmc.t-handy)
                        (def a (org.timmc.t-handy/resolver Shape))]
           (+ 2 3))
         5)))

;;;; Structural

(deftest indexing
  (is (= (index-on [{:a 0, :b 1, :c 2}, {:a 3, :b 4, :c 5}] (juxt :a :b) [:c])
         {[0 1] {:c 2}, [3 4] {:c 5}})))

;;;; Mutation

(deftest splitting
  (let [a (atom (range 10))]
    (is (= (split-atom! a first rest) 0))
    (is (= @a (range 1 10)))))

;;;; Testing

(deftest calvinism
  (let [f (deterministic 0 1 2 3 4)]
    (is (= (map f [:a :b :c :d]) (range 0 4)))
    (is (= (f 'various 'things) 4))))

;;;; Calculations

(deftest pagination
  (testing "error conditions"
    (are [total cp result] (= (paging total cp 10)
                              (assoc result
                                :total total, :cur-page cp, :per-page 10))
         ;; empty -- always invalid
         0 0 {:has-records false, :cur-valid false}
         0 5 {:has-records false, :cur-valid false}
         ;; out of bounds
         25 3 {:has-records true, :cur-valid false
               :first-page 0, :last-page 2
               :last-page-size 5}
         30 3 {:has-records true, :cur-valid false
               :first-page 0, :last-page 2
               :last-page-size 10}))
  (testing "normal conditions (3 pages)"
    (let [constant {:has-records true, :cur-valid true,
                    :first-page 0, :last-page 2}]
      (are [total cp result] (= (paging total cp 10)
                                (assoc (into result constant)
                                  :total total, :cur-page cp, :per-page 10))
           ;; ragged results (last page not full)
           21 2 {:has-prev true, :has-next false
                 :first-record 20, :last-record 20
                 :last-page-size 1}
           25 0 {:has-prev false, :has-next true
                 :first-record 0, :last-record 9
                 :last-page-size 5}
           25 1 {:has-prev true, :has-next true
                 :first-record 10, :last-record 19
                 :last-page-size 5}
           25 2 {:has-prev true, :has-next false
                 :first-record 20, :last-record 24
                 :last-page-size 5}
           ;; non-ragged results
           30 2 {:has-prev true, :has-next false
                 :first-record 20, :last-record 29
                 :last-page-size 10}))))
