(ns require-viz.main
  (:require
   [clojure.java.io              :as io]
   [clojure.tools.namespace.find :as ctnf]
   [loom.graph]
   [loom.io]))

(defn extract-child-nss [ns-decl]
  (some->> ns-decl
           ;; find :require seq
           (filter coll?)
           (filter #(= (first %) :require))
           (first)
           ;; keep only declarations
           (drop 1)
           ;; drop :as, :refer, etc keeping only full ns declaration
           (map #(if (vector? %) (first %) %))))

(defn ns-decl->edges [ns-decl]
  (let [parent-ns (nth ns-decl 1)
        child-nss (extract-child-nss ns-decl)]
    (map #(vector parent-ns %) child-nss)))

(defn -main
  "Render the digraph representing the clj(s) namespace declarations in
   `src-path` to a DOT file at path `out-dot`.

   To render, drag onto OmniGraffle or (assuming graphviz installed):
   `dot -Tsvg out-dot > out.svg`"
  [out-dot src-path]
  (let [declarations (ctnf/find-ns-decls-in-dir (io/file src-path))
        edges        (mapcat ns-decl->edges declarations)
        dg           (apply loom.graph/digraph edges)]
    (loom.io/dot dg out-dot)))

(comment
  (-main "grid.dot" "/Users/rgm/Projects/opengb/grid/systems/grid/src/"))
