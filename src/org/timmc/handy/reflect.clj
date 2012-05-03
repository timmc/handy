(ns org.timmc.handy.reflect
  (:import (java.lang.reflect Member Field Method Constructor Modifier))
  (:refer-clojure :exclude (methods)))

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

(defn fields
  "Return fields as maps of {:type :field, :name String, :return Class,
:visibility? kw, :static? bool}"
  ([^Class c, opts]
     (if (:ancestors opts)
       (mapcat #(fields % (dissoc opts :ancestors))
               (conj (ancestors c) c))
       (for [m (.getDeclaredFields c)]
         (let [mod (.getModifiers m)]
           {:type :field, :name (.getName m), :visibility (visibility m),
            :static? (Modifier/isStatic mod), :return (.getType m)}))))
  ([^Class c] (fields c {})))

(defn constructors
  "Return constructors as maps of {:type :constructor, :params [Class...],
:varargs? bool, :visibility? kw}"
  ([^Class c, opts]
     (if (:ancestors opts)
       (mapcat #(constructors % (dissoc opts :ancestors))
               (conj (ancestors c) c))
       (for [m (.getDeclaredConstructors c)]
         (let [mod (.getModifiers m)]
           {:type :constructor, :params (vec (.getParameterTypes m)),
            :varargs? (.isVarArgs m), :visibility (visibility m)}))))
  ([^Class c] (constructors c {})))

(defn methods
  "Return methods as maps of {:type :method, :name String, :return Class,
:params [Class...], :varargs? bool, :abstract? bool, :visibility? kw,
:static? bool}"
  ([^Class c, opts]
     (if (:ancestors opts)
       (mapcat #(methods % (dissoc opts :ancestors))
               (conj (ancestors c) c))
       (for [m (.getDeclaredMethods c)]
         (let [mod (.getModifiers m)]
           {:type :method, :name (.getName m), :return (.getReturnType m),
            :visibility (visibility m), :abstract? (Modifier/isAbstract mod),
            :params (vec (.getParameterTypes m)), :varargs? (.isVarArgs m),
            :static? (Modifier/isStatic mod)}))))
  ([^Class c] (methods c {})))
