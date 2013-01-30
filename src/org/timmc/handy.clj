(ns org.timmc.handy
  "Main utility namespace.")

;;;; Comparisons

(defn ^{:since "1.0.0"} lexicomp
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

(defn ^{:since "1.0.0"} version-norm
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

(defn ^{:since "1.0.0"} version<=
  "Check that the versions are in monotonically increasing order (does not
require strictly ascending.) Versions are strings of 1 or more dot-delimited
integers. Trailing zeros will be ignored, as with 'version-norm. Returns
logical true/false."
  [v & more]
  {:pre [(string? v), (every? string? more)]}
  (let [vs (map version-norm (cons v more))]
    (every? (complement pos?) (map lexicomp vs (next vs)))))

;;;; Sandboxing

(defmacro ^{:since "1.1.0"} with-temp-ns
  "Run some code in a namespace sandbox."
  [[& ns-modifiers] & exprs]
  (let [old-ns (.name *ns*)
        tmp-ns (gensym 'sandbox)]
    (list
     'do ;; namespace modification must occur in a top-level 'do
     `(in-ns '~tmp-ns)
     '(clojure.core/refer 'clojure.core)
     ;; eval allows us to 1) catch compile errors, and 2) put both ns-modifying
     ;; and ns-using forms in the same top-level form (for conciseness.)
     `(try
        (eval '~(cons 'do ns-modifiers))
        (eval '~(cons 'do exprs))
        (finally
         (in-ns '~old-ns)
         (remove-ns '~tmp-ns))))))

;;;; Structural manipulation

(defn ^{:since "1.2.0"} index-on
  "From a table (coll of record maps) produce a map of index key values
to projections on the other keys. r->k is a function of a record to some
key value, e.g. #(get % 5) or (juxt :a :b) or just :c.

Example: (index-on [{:a 0, :b 1, :c 2}, {:a 3, :b 4, :c 5}] :a [:b])
         => {0 {:b 1}, 3 {:b 4}}"
  [table r->k keep-keys]
  (into {} (for [record table]
             [(r->k record) (select-keys record keep-keys)])))

;;;; Mutation

(defn ^{:since "1.3.0"} split-atom!
  "Swap an atom with the `keep` function and produce the corresponding
result of applying `return` to the old value. Thread-safe.

This is useful if you want to remove a value from a collection in an atom
but also return the value."
  [a return keep]
  (let [r (atom nil)]
    (swap! a (fn splitter [old]
               (reset! r (return old))
               (keep old)))
    @r))

;;;; Testing

(defn ^{:since "1.3.0"} deterministic
  "Return a function that will return each of the given values in
sequence when called multiple times. Behavior unspecified if called
more times than there are values.

Recommended for use in combination with with-redefs."
  [& returns]
  (let [remaining (atom returns)]
    (fn determined [& _] (split-atom! remaining first rest))))
