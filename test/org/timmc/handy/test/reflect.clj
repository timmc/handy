(ns org.timmc.handy.test.reflect
  (:use clojure.test
        org.timmc.handy.reflect)
  (:refer-clojure :exclude (methods)))

(deftest vis
  (is (= (visibility (.getMethod String "equals" (into-array Class [Object])))
         :public))
  (is (vis>= :public :public))
  (is (vis>= :public :package))
  (is (not (vis>= :private :protected))))

(deftest deduplication
  (let [subject (partial dedupe first #(> (second %) (second %2)))]
    (testing "base cases"
      (is (= (subject [])
             []))
      (is (= (subject [[:a 1]])
             [[:a 1]])))
    (testing "order-independence in 2-element case (regression check)"
      (is (= (subject [[:a 3] [:a 1]])
             [[:a 3]]))
      (is (= (subject [[:a 1] [:a 3]])
             [[:a 3]])))
    (testing "mixed order, with full-duplicate checking"
      (is (= (sort-by first compare
                      (subject [[:a 1] [:b 20 3] [:a 5] [:b 20 1] [:a 0]]))
             [[:a 5] [:b 20 3]])))))

(deftest field
  (is (= (first (filter (comp #{"SIZE"} :name) (fields Long)))
         {:type :field, :name "SIZE", :return Integer/TYPE,
          :visibility :public, :static? true, :synthetic? false, :owner Long})))

(deftest constr
  (is (= (first (constructors Enum))
         {:type :constructor, :visibility :protected, :varargs? false,
          :params [String Integer/TYPE], :synthetic? false, :owner Enum})))

(deftest meth
  (is (= (first (filter (comp #{"equalsIgnoreCase"} :name) (methods String)))
         {:type :method, :name "equalsIgnoreCase", :return Boolean/TYPE,
          :visibility :public, :abstract? false, :params [String],
          :varargs? false, :static? false, :synthetic? false, :owner String})))

(deftest ancestry
  (let [example (class {})
        ;; best we can do in the face of method overrides
        signature (juxt :type :name :params)]
    (are [f] (= (set (map signature
                          (f example {:ancestors true})))
                (set (map signature
                          (mapcat f (cons example (ancestors example))))))
         fields
         constructors
         methods)))
