(ns org.timmc.handy.reflect
  "Synthetic members are omitted unless the :show-synthetic option is
enabled. Inherited members are omitted unless the :ancestors option is
enabled."
  (:require [org.timmc.handy :as core])
  (:import (java.lang.reflect Member Field Method Constructor Modifier))
  (:refer-clojure :exclude (dedupe methods)))

(def ^:private vis-levels {:public 3 :protected 2 :package 1 :private 0})

(defn ^:internal vis>=
  [to-check threshold]
  (apply >= (map vis-levels [to-check threshold])))

(defn ^{:added "1.5.0"} visibility
  "Return Member visibility as :public, :protected, :package, or :private."
  [^Member m]
  (let [mod (.getModifiers m)]
    (cond (Modifier/isPublic mod) :public
          (Modifier/isProtected mod) :protected
          (Modifier/isPrivate mod) :private
          :else :package)))

;; Internal because it needs work.
(defn ^:internal dedupe
  "Given zero or more values, remove duplicates. keyfn maps values to keys (in
the DB sense) -- matching return values mean the inputs are duplicates.
beats? [a b -> bool] resolves the conflicts -- a logical-true return
indicates that a is preferred in deduplication with b."
  [keyfn beats? vs]
  (for [[_ conflict] (group-by keyfn vs)]
    (if (= (count conflict) 1)
      (first conflict)
      ;; Using beats? this way prefers the first element in the case of
      ;; values that are duplicates according to both keyfn and beats?.
      (reduce #(if (beats? %2 %1) %2 %1) conflict))))

;; If I ever try to add functionality to indicate whether a method
;; overrides another method, make sure to take into account bridge
;; methods and covariant return types.

(defn ^:internal recurse-members
  "Run the members function f (with given options map opts) against c
and all of its ancestors, then remove shadowed members."
  [f c opts]
  (dedupe (juxt :type :name :params)
          #(isa? (:owner %1) (:owner %2))
          (mapcat #(f % (dissoc opts :ancestors))
                  (cons c (ancestors c)))))

(defn ^{:added "1.5.0"} fields
  "Return fields as maps of {:type :field, :name String, :return Class,
:visibility? kw, :static? bool, :owner Class, :synthetic? bool}"
  ([^Class c, opts]
     (if (:ancestors opts)
       (recurse-members fields c opts)
       (for [m (.getDeclaredFields c)]
         (let [mod (.getModifiers m)]
           {:type :field, :name (.getName m), :visibility (visibility m),
            :static? (Modifier/isStatic mod), :return (.getType m),
            :owner (.getDeclaringClass m), :synthetic? (.isSynthetic m)}))))
  ([^Class c] (fields c {})))

(defn ^{:added "1.5.0"} constructors
  "Return constructors as maps of {:type :constructor, :params [Class...],
:varargs? bool, :visibility? kw, :owner c, :synthetic? bool}."
  ([^Class c, opts]
     (if (:ancestors opts)
       (recurse-members constructors c opts)
       (for [m (.getDeclaredConstructors c)
             :when (or (not (.isSynthetic m)) (:show-synthetic opts))]
         (let [mod (.getModifiers m)]
           {:type :constructor, :params (vec (.getParameterTypes m)),
            :varargs? (.isVarArgs m), :visibility (visibility m),
            :owner (.getDeclaringClass m), :synthetic? (.isSynthetic m)}))))
  ([^Class c] (constructors c {})))

(defn ^{:added "1.5.0"} methods
  "Return methods as maps of {:type :method, :name String, :return Class,
:params [Class...], :varargs? bool, :abstract? bool, :visibility? kw,
:static? bool, :owner c, :synthetic? bool}."
  ([^Class c, opts]
     (if (:ancestors opts)
       (recurse-members methods c opts)
       (for [m (.getDeclaredMethods c)
             :when (or (not (.isSynthetic m)) (:show-synthetic opts))]
         (let [mod (.getModifiers m)]
           {:type :method, :name (.getName m), :return (.getReturnType m),
            :visibility (visibility m), :abstract? (Modifier/isAbstract mod),
            :params (vec (.getParameterTypes m)), :varargs? (.isVarArgs m),
            :static? (Modifier/isStatic mod), :owner (.getDeclaringClass m),
            :synthetic? (.isSynthetic m)}))))
  ([^Class c] (methods c {})))
