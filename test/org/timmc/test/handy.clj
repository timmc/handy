(ns org.timmc.test.handy
  (:use [org.timmc.handy])
  (:use [clojure.test]))

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
                        (require 'org.timmc.test.handy)
                        (def a (org.timmc.test.handy/resolver Shape))]
           (+ 2 3))
         5)))

;;;; Structural

(deftest indexing
  (is (= (index-on [{:a 0, :b 1, :c 2}, {:a 3, :b 4, :c 5}] (juxt :a :b) [:c])
         {[0 1] {:c 2}, [3 4] {:c 5}})))
