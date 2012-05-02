(ns org.timmc.handy.reflect
  (:import (java.lang.reflect Member Field Method Constructor Modifier)))


(def ^:private vis-levels {:public 3 :protected 2 :package 1 :private 0})

(defn vis>=
  [to-check threshold]
  (apply >= (map vis-levels [to-check threshold])))

(defn visibility
  "Return Member visibility as :public, :protected, :package, or :private."
  [^Member m]
  (let [mod (.getModifiers m)]
    (cond (Modifier/isPublic mod) :public
          (Modifier/isProtected mod) :protected
          (Modifier/isPrivate mod) :private
          :else :package)))

(defn methods
  "Return methods as maps of {:type :method, :name String, :return Class,
:params [Class...], :varargs? bool, :abstract? bool, :visibility? kw}"
  ([^Class c, opts]
     (if (:ancestors opts)
       (mapcat #(methods % (dissoc opts :ancestors))
               (conj (ancestors c) c))
       (for [m (.getDeclaredMethods c)]
         (let [mod (.getModifiers c)]
           {:type :method, :name (.getName m), :return (.getReturnType m),
            :visibility (visibility m), :abstract? (Modifier/isAbstract mod),
            :params (vec (.getParameterTypes m)), :varargs? (.isVarArgs m)}))))
  ([^Class c] (methods c {})))
