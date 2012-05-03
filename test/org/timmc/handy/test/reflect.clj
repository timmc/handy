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

(deftest field
  (is (= (first (filter (comp #{"SIZE"} :name) (fields Long)))
         {:type :field, :name "SIZE", :return Integer/TYPE,
          :visibility :public, :static? true})))

(deftest constr
  (is (= (first (constructors Enum))
         {:type :constructor, :visibility :protected, :varargs? false,
          :params [String Integer/TYPE]})))

(deftest meth
  (is (= (first (filter (comp #{"equalsIgnoreCase"} :name) (methods String)))
         {:type :method, :name "equalsIgnoreCase", :return Boolean/TYPE,
          :visibility :public, :abstract? false, :params [String],
          :varargs? false, :static? false})))

(deftest ancestry
  (let [example (class {})]
    (are [f] (= (set (f example {:ancestors true}))
                (set (mapcat f (cons example (ancestors example)))))
         fields
         constructors
         methods)))
