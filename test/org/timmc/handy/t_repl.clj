(ns org.timmc.handy.t-repl
  (:use clojure.test
        org.timmc.handy.repl))

(deftest formatting
  (are [c fq? s] (binding [*show-fq* fq?]
                   (= (format-classname c) s))
       String false "String"
       String true "java.lang.String"
       Integer/TYPE false "int"
       Integer/TYPE true "int"
       (class (int-array [])) false "int[]"
       (class (int-array [])) true "int[]"
       (class (into-array [(into-array Void [])])) false "Void[][]"
       (class (into-array [(into-array Void [])])) true "java.lang.Void[][]"))
