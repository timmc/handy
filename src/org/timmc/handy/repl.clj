(ns org.timmc.handy.repl
  (:use [clojure.reflect :only (reflect)])
  (:require [clojure.string :as str])
  (:import (clojure.reflect Field Method Constructor)))

(def ^:private vis-levels {:public 3 :protected 2 :package 1 :private 0})

(defn- vis>=
  [to-check threshold]
  (apply >= (map vis-levels [to-check threshold])))

(defn- vis
  [m]
  (or (some #{:public :private :protected} (:flags m)) :package))

(defn- vis-str
  [m]
  (.substring (name (vis m)) 0 3))

(defn- static-str
  [m]
  (if (:static (:flags m)) "st" "  "))

(def ^:private ^:dynamic *fq* false)

(defn- format-classname
  [cls]
  (cond (= cls 'void) "void"
        (class? cls) (if *fq* (.getName cls) (.getSimpleName cls))
        (symbol? cls) (format-classname (resolve cls))
        (var? cls) (.sym cls) ;; cast fn, such as #'clojure.core/long
        :else cls))

(defmulti write-member type)
(defmethod write-member Field [f]
  (format " %s %s %s : %s"
          (vis-str f)
          (static-str f)
          (:name f)
          (format-classname (:type f))))
(defmethod write-member Constructor [c]
  (format " %s : %s"
          (vis-str c)
          (str/join ", " (map format-classname (:parameter-types c)))))
(defmethod write-member Method [m]
  (format " %s %s %s : %s : %s"
          (vis-str m)
          (static-str m)
          (:name m)
          (format-classname (:return-type m))
          (str/join ", " (map format-classname (:parameter-types m)))))

(defn- sort-members
  [ms]
  (sort-by (comp not :static :flags) (sort-by :name ms)))

(defn show
  "Print the methods, constructors, and fields of the class of the provided
value (or the class itself, if a Class subclass is provided.) Specs are
keyword arguments, with allowed values:
* :level <lvl> - Minimum visibility of members to show. lvl may be :public
  (the default), :protected, :package, or :private.
* :fq <bool> - If true, show fully-qualified classnames. Default false."
  [v & {:as specs
        :keys [level fq]
        :or {level :public, fq false}}]
  {:pre [(contains? #{:public :protected :package :private} level)]}
  (let [cl (if (class? v) v (class v))
        data (reflect v)
        members (group-by type (:members data))]
    (binding [*fq* fq]
      (println (format-classname cl))
      (println "Bases:" (map format-classname (:bases data)))
      (doseq [t [Field Constructor Method]]
        (when-let [ms (filter #(vis>= (vis %) level) (members t))]
          (println (str (.getSimpleName t) "s:"))
          (doseq [m (sort-members ms)]
            (println (write-member m))))))
    (symbol "") ;; stupid hack to not show a nil after the printout
    ))
