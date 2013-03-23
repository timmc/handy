(ns org.timmc.handy.repl
  (:require [clojure.string :as str]
            [org.timmc.handy.reflect :as rf]
            [clojure.set :as set]))

(defn- vis-str
  [kw]
  (.substring (name kw) 0 3))

(defn- static-str
  [m]
  (if (:static? m) "st" "  "))

(def ^:dynamic *fq* "True for fully-qualified classnames in output."
  false)

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
  (format " %s %s %s : %s -> %s"
          (vis-str (:visibility m))
          (static-str m)
          (:name m)
          (str/join ", " (map format-classname (:params m)))
          (format-classname (:return m))))

(defn- sort-members
  [ms]
  (sort-by (comp not :static?) (sort-by :name ms)))

(defn show
  "Print the methods, constructors, and fields of the class of the provided
value (or the class itself, if a Class subclass is provided.) Options are
entered in an optional map, with allowed values:
* :level <lvl> - Minimum visibility of members to show. lvl may be :public
  (the default), :protected, :package, or :private.
* :fq <bool> - If true, show fully-qualified classnames. Default false.
* :return <class> - If set, restrict output to fields and methods whose return
  value is assignable to this type.
* :inherit <bool> - If true, include inherited members."
  ([v {:as specs
       :keys [level fq return inherit]
       :or {level :public, fq false, return nil, inherit false}}]
     {:pre [(contains? #{:public :protected :package :private} level)
            (or (nil? return) (class? return))]}
     (let [cl (if (class? v) v (class v))
           rf-opts (set/rename-keys (dissoc specs [:level :fq :return])
                                    {:inherit :ancestors})
           members {:Fields (rf/fields cl rf-opts)
                    :Constructors (rf/constructors cl rf-opts)
                    :Methods (rf/methods cl rf-opts)}]
       (binding [*fq* fq]
         (println (format-classname cl))
         (println "Bases:" (map format-classname (bases cl)))
         (doseq [t [:Fields :Constructors :Methods]]
           (println (str (name t) ":"))
           (let [filter-return (if (:return specs)
                                 (partial filter #(isa? (:return %) return))
                                 identity)
                 ms (->> (get members t :BUG_HERE)
                         (filter #(rf/vis>= (:visibility %) level))
                         filter-return)]
             (doseq [m (sort-members ms)]
               (println (write-member m))))))
       (symbol "") ;; stupid hack to not show a nil after the printout
       ))
  ([v] (show v {})))
