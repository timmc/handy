(ns org.timmc.handy
  "Main utility namespace.")

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
