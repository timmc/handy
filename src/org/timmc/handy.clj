(ns org.timmc.handy
  "Main utility namespace: A grab-bag of handy things.")

;;;; Control flow

(defmacro ^{:added "1.4.0"} if-let+
  "Like if-let, but with multiple bindings. If any binding evaluates to
false or nil, the else expression is evaluated and returned, and the
remaining bindings are not evaluated. The else-expression cannot use any
of the bindings, and the then-expression may use all of them."
  [bindings then-expr else-expr]
  (when-not (vector? bindings)
    (throw (RuntimeException. "if-let+ requires a vector for the bindings")))
  (when-not (even? (count bindings))
    (throw (RuntimeException. "if-let+ bindings count must be even")))
  (let [else-sym (gensym "else_")]
    `(let [~else-sym (fn delay-else [] ~else-expr)]
       ~(reduce
         (fn [core clause]
           ;; fully-qualify if-let in case we decide to rename this macro
           ;; to if-let in the future.
           `(clojure.core/if-let [~@clause] ~core (~else-sym)))
         then-expr
         (reverse (partition 2 bindings))))))

;;;; Comparisons

(defn ^{:added "1.0.0"} lexicomp
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

(defn ^{:added "1.0.0"} version-norm
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

(defn ^{:added "1.0.0"} version<=
  "Check that the versions are in monotonically increasing order (does not
require strictly ascending.) Versions are strings of 1 or more dot-delimited
integers. Trailing zeros will be ignored, as with 'version-norm. Returns
logical true/false."
  [v & more]
  {:pre [(string? v), (every? string? more)]}
  (let [vs (map version-norm (cons v more))]
    (every? (complement pos?) (map lexicomp vs (next vs)))))

;;;; Reflection

;; Inspired by https://github.com/technomancy/leiningen/blob/5e26a1639f0bc0a7efdc078f1a5011af92b2cd70/leiningen-core/src/leiningen/core/main.clj#L226 viewed 2014-05-08
(defn ^{:added "1.6.0"} matching-arity
  "Given a collection of arglists (or a var or a var's metadata) and
an arity count to test aginst, yield smallest matching arglist or
nil." ;; TODO Accept actual arglist, check types?
  [arglist-source arity]
  ;; I'm sure there's some horribly fun use of function composition I
  ;; could do here, building up a list of unwrappers... but I won't do
  ;; it, because I'm nice.
  (let [arglists (cond (var? arglist-source) (:arglists (meta arglist-source))
                       (map? arglist-source) (:arglists arglist-source)
                       :else                 arglist-source)]
    (first (filter (fn [arglist]
                     (if (= '& (last (butlast arglist)))
                       (<= (- (count arglist) 2) arity)
                       (= (count arglist) arity)))
                   ;; Get [a b] before [a b & c]
                   (sort arglists)))))

;;;; Sandboxing

(defmacro ^{:added "1.1.0"} with-temp-ns
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

(defn ^{:added "1.2.0"} index-on
  "From a table (coll of record maps) produce a map of index key values
to projections on the other keys. r->k is a function of a record to some
key value, e.g. #(get % 5) or (juxt :a :b) or just :c.

Example: (index-on [{:a 0, :b 1, :c 2}, {:a 3, :b 4, :c 5}] :a [:b])
         => {0 {:b 1}, 3 {:b 4}}"
  [table r->k keep-keys]
  (into {} (for [record table]
             [(r->k record) (select-keys record keep-keys)])))

;;;; Mutation

(defn ^{:added "1.3.0"} split-atom!
  "Swap an atom with the `keep` function and produce the corresponding
result of applying `return` to the old value. Thread-safe.

This is useful if you want to remove a value from a collection in an atom
but also return the value.

If `return` throws, the atom is left unchanged (and the exception
propagates.)"
  [a return keep]
  (let [r (atom nil)]
    (swap! a (fn splitter [old]
               (reset! r (return old))
               (keep old)))
    @r))

;;;; Testing

(defn ^{:added "1.3.0"} deterministic
  "Return a function that will return each of the given values in
sequence when called multiple times. Behavior unspecified if called
more times than there are values.

Recommended for use in combination with with-redefs."
  [& returns]
  (let [remaining (atom returns)]
    (fn determined [& _] (split-atom! remaining first rest))))

;;;; Calculations

(defn ^{:added "1.4.0"} paging
  "Derive paging information from a record count and requested page index.

Input:

* `total` - Total number of records available for paging through.
* `cur-page` - Requested page index, 0-based. Must be non-negative integer.
* `per-page` - Number of records to display per page.

The output is a map. The following keys are always present:

* `:total` - From input.
* `:cur-page` - From input.
* `:per-page` - From input.
* `:has-records` - True iff `total` was not 0.
* `:cur-valid` - True iff `cur-page` indexes a page containing results.

If `total` is not 0, the following keys will also be present:

* `:first-page` - Index of first page (always 0.)
* `:last-page` - Index of last page containing records.
* `:last-page-size` - Number of records on last page.

If `:cur-valid` is true (`total` is not 0 and `cur-page` is in bounds), the
following keys will also be present:

* `:has-prev` - True iff `(dec cur-page)` is a valid page index.
* `:has-next` - True iff `(inc cur-page)` is a valid page index.
* `:first-record` - Offset of first record on page, 0-based. Page 0 starts with
  record 0.
* `:last-record` - Offset of last record on page, 0-based.

No other keys will be present."
  [total cur-page per-page]
  {:pre [(not (neg? cur-page)), (not (neg? total)), (pos? per-page)]}
  (assoc
      (let [has-records (not (zero? total))
            full-pages (quot total per-page)
            last-page-size (rem total per-page) ;; may be 0 & need to recalc
            last-page-full? (zero? last-page-size)
            last-page-size (if last-page-full? per-page last-page-size)
            last-page (if last-page-full? (dec full-pages) full-pages)
            on-page (if (= cur-page last-page) last-page-size per-page)
            in-bounds (<= 0 cur-page last-page)
            first-record (* per-page cur-page)
            last-record (+ first-record (dec on-page))]
        (if-not has-records
          {:has-records false
           :cur-valid false}
          (let [baseline {:has-records true
                          :first-page 0
                          :last-page last-page
                          :last-page-size last-page-size}]
            (if-not in-bounds
              ;; just return overall bounds information
              (assoc baseline :cur-valid false)
              ;; if page is valid, add info on page stats
              (assoc baseline
                :cur-valid true
                :has-prev (not= cur-page 0)
                :has-next (not= cur-page last-page)
                :first-record first-record
                :last-record last-record)))))
    ;; always attach the inputs
    :total total
    :cur-page cur-page
    :per-page per-page))
