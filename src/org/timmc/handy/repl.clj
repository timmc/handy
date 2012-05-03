(ns org.timmc.handy.repl
  (:require [clojure.string :as str]
            [org.timmc.handy.reflect :as rf]))

(defn- vis-str
  [kw]
  (.substring (name kw) 0 3))

(defn- static-str
  [m]
  (if (:static? m) "st" "  "))

(def ^:dynamic *fq* false)

(defn ^:internal format-classname
  [cls]
  (if *fq*
    (if (.isPrimitive cls)
      (.getSimpleName cls)
      (if (.isArray cls)
        (str (format-classname (.getComponentType cls)) "[]")
        (.getName cls)))
    (.getSimpleName cls)))

(defmulti write-member :type)
(defmethod write-member :field [f]
  (format " %s %s %s : %s"
          (vis-str (:visibility f))
          (static-str f)
          (:name f)
          (format-classname (:return f))))
(defmethod write-member :constructor [c]
  (format " %s : %s"
          (vis-str (:visibility c))
          (str/join ", " (map format-classname (:params c)))))
(defmethod write-member :method [m]
  (format " %s %s %s : %s : %s"
          (vis-str (:visibility m))
          (static-str m)
          (:name m)
          (format-classname (:return m))
          (str/join ", " (map format-classname (:params m)))))

(defn- sort-members
  [ms]
  (sort-by (comp not :static?) (sort-by :name ms)))

;;(defmulti filter-return type)

(defn show
  "Print the methods, constructors, and fields of the class of the provided
value (or the class itself, if a Class subclass is provided.) Options are
entered in an optional map, with allowed values:
* :level <lvl> - Minimum visibility of members to show. lvl may be :public
  (the default), :protected, :package, or :private.
* :fq <bool> - If true, show fully-qualified classnames. Default false."
  ([v {:as specs
       :keys [level fq]
       :or {level :public, fq false}}]
     {:pre [(contains? #{:public :protected :package :private} level)]}
     (let [cl (if (class? v) v (class v))
           members {:Fields (rf/fields cl)
                    :Constructors (rf/constructors cl)
                    :Methods (rf/methods cl)}]
       (binding [*fq* fq]
         (println (format-classname cl))
         (println "Bases:" (map format-classname (bases cl)))
         (doseq [t [:Fields :Constructors :Methods]]
           (when-let [ms (filter #(rf/vis>= (:visibility %) level)
                                 (get members t :DONE_BROKE))]
             (println (str (name t) ":"))
             (doseq [m (sort-members ms)]
               (println (write-member m))))))
       (symbol "") ;; stupid hack to not show a nil after the printout
       ))
  ([v] (show v {})))
