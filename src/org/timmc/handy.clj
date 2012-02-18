(ns org.timmc.handy
  "Main utility namespace.")

;;;; Comparisons

(defn lexicomp
  "Compare two sequential collections lexicographically, returning a
positive integer if the first argument is greater than the other
argument, as in Java's Comparable. (Negative for other order, zero for equal.)
nil sorts the same as the empty collection. Comparison is shortcutting (will
do as little computation as possible.)"
  [main other]
  {:pre [(or (nil? main) (sequential? main)), ;;TODO(drop-1.2): some-fn
         (or (nil? other) (sequential? other))]
   :post [(number? %)]}
  (loop [main (seq main)
         other (seq other)]
    (if (nil? main)
      (if (nil? other) 0 -1)
      (if (nil? other)
        1
        (let [cmp (.compareTo (first main) (first other))]
          (if (zero? cmp)
            (recur (next main) (next other))
            cmp))))))

(defn version-norm
  "Convert a simple version into a sequential coll of integers with no
trailing zero elements. For example, \"2.21.6.0.0\" => [2 21 6]. A version
string is a dot-separated series of one or more integers."
  [s]
  {:pre [(string? s)]
   :post [(sequential? %), (every? integer? %)]}
  (->> s
       (re-seq #"\d+")
       (map #(Integer/parseInt % 10))
       reverse
       (drop-while zero?)
       reverse))

(defn version<=
  "Check that the versions are in monotonically increasing order (does not
require strictly ascending.) Versions are strings of 1 or more dot-delimited
integers. Trailing zeros will be ignored, as with 'version-norm. Returns
logical true/false."
  [v & more]
  {:pre [(string? v), (every? string? more)]}
  (let [vs (map version-norm (cons v more))]
    (every? (complement pos?) (map lexicomp vs (next vs)))))

;;;; Sandboxing

(defmacro in-temp-ns
  "Run some code in a namespace sandbox. Must be a top-level form."
  [[& ns-modifiers] & exprs]
  (let [old-ns (.name *ns*)
        tmp-ns (gensym 'sandbox)
        restore `((in-ns '~old-ns)
                  (remove-ns '~tmp-ns))]
    (list
     'do ;; namespace modification must occur in a top-level 'do
     `(in-ns '~tmp-ns)
     '(clojure.core/refer 'clojure.core)
     ;; eval allows us to catch compile errors
     `(try
        (eval '~(cons 'do ns-modifiers))
        (catch Throwable t#
          ~@restore
          (throw (Throwable. t#))))
     ;; now that namespace has been modified, this second top-level form
     ;; can take advantage of the new bindings
     `(try
        (eval '~(cons 'do exprs))
        (finally
         ~@restore)))))
