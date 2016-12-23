(ns org.timmc.handy.t-reflect
  (:require [org.timmc.handy.reflect :as r])
  (:use clojure.test))

(deftest vis
  (is (= (r/visibility (.getMethod String "equals" (into-array Class [Object])))
         :public))
  (is (r/vis>= :public :public))
  (is (r/vis>= :public :package))
  (is (not (r/vis>= :private :protected))))

(deftest deduplication
  (let [subject (partial r/dedupe first #(> (second %) (second %2)))]
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
  (is (= (first (filter (comp #{"SIZE"} :name) (r/fields Long)))
         {:type :field, :name "SIZE", :return Integer/TYPE,
          :visibility :public, :static? true, :synthetic? false, :owner Long})))

(deftest constr
  (is (= (first (r/constructors Enum))
         {:type :constructor, :visibility :protected, :varargs? false,
          :params [String Integer/TYPE], :synthetic? false, :owner Enum})))

(deftest meth
  (is (= (first (filter (comp #{"equalsIgnoreCase"} :name) (r/methods String)))
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
         r/fields
         r/constructors
         r/methods)))
