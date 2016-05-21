(ns webjure.json-schema.ref
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :as io]))

(defn resolve-ref [uri]
  (let [schema (io/file uri)]
    (when (.canRead schema)
      (cheshire/parse-string (slurp schema)))))

(def definition-pattern #"#/definitions/(.+)$")

(defn definition [{definitions :definitions} ref]
  (when-let [[_ definition-name] (re-find definition-pattern ref)]
    (get definitions definition-name)))

(defn resolve-schema [schema options]
  (if-let [ref (get schema "$ref")]
    (or (definition options ref)
        (let [resolver (or (:ref-resolver options)
                           resolve-ref)
              referenced-schema (resolver ref)]
          (if-not referenced-schema
            ;; Throw exception if schema can't be loaded
            (throw (IllegalArgumentException.
                    (str "Unable to resolve referenced schema: " ref)))
            referenced-schema)))
    schema))
