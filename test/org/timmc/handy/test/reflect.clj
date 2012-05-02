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
  (let [c (class {})]
    (is (= (set (methods c {:ancestors true}))
           (set (mapcat methods (cons c (ancestors c))))))))
