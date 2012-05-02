(ns org.timmc.handy.test.reflect
  (:use clojure.test
        org.timmc.handy.reflect))

(deftest vis
  (is (= (visibility (.getMethod String "equals" (into-array Class [Object])))
         :public))
  (is (vis>= :public :public))
  (is (vis>= :public :package))
  (is (not (vis>= :private :protected))))

(deftest meth
  (is (= (first (filter #(= (:name %) "equalsIgnoreCase") (methods String)))
         {:type :method, :name "equalsIgnoreCase", :return Boolean/TYPE,
          :visibility :public, :abstract? false, :params [String],
          :varargs? false}))
  (is (= (methods Void) []))
  (is (= (methods Object) (methods Void {:ancestors true}))))
