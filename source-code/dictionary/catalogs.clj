(ns dictionary.catalogs
  "The catalogs on the server: read from the classpath, each language's
   <catalogs-path>/<language>.json, and a message made into text by ICU4J,
   in the language handed in as a value (no dynamic language: an email or
   a PDF passes the language of the row it is made for)."
  (:require
   [clojure.java.io :as io]
   [clojure.set :as set]
   [dictionary.settings :as settings]
   [dictionary.texts :as texts]
   [jsonista.core :as json])
  (:import
   (com.ibm.icu.text MessageFormat)
   (com.ibm.icu.util ULocale)
   (java.util HashMap Map)))

(defn list-catalog-files
  "Every language that shows, with its catalog's classpath resource and
   the address the browser fetches it at: [{:language \"de\" :resource
   \"i18n/de.json\" :address \"/i18n/de.json\"} …]."
  [{:keys [catalogs-path] :as settings}]
  (mapv (fn [language]
          {:language language
           :resource (str catalogs-path "/" language ".json")
           :address  (str "/" catalogs-path "/" language ".json")})
        (settings/list-live-languages settings)))

(defn- read-catalog [resource]
  (when-let [url (io/resource resource)]
    (json/read-value (slurp url))))

(defn load-texts
  "Every language that shows, its catalog read from the classpath: the
   texts (dictionary.texts). A catalog that isn't there throws, naming
   each."
  [{:keys [fallback-language] :as settings}]
  (let [files   (list-catalog-files settings)
        read    (into {} (map (fn [{:keys [language resource]}] [language (read-catalog resource)])) files)
        missing (vec (for [{:keys [language resource]} files :when (nil? (get read language))] resource))]
    (when (seq missing)
      (throw (ex-info (str "No catalog on the classpath for " (pr-str missing))
                      {:type :dictionary/missing-catalogs :resources missing})))
    {:fallback-language fallback-language :catalogs read}))

(defn- format-message [^String message ^String language args]
  (.format (MessageFormat. message (ULocale. language)) ^Map (HashMap. ^Map (texts/turn-args-into-names args))))

(defn make-text
  "The key's message in the language, its ICU arguments filled in from
   args ({:count 3}, {} for none); in the fallback language when the
   language's catalog lacks it; the key's name when neither has it."
  [texts language message-key args]
  (if-let [{:keys [message] found-in :language} (texts/find-message texts language message-key)]
    (format-message message found-in args)
    (texts/turn-key-into-catalog-key message-key)))

(defn- find-bad-messages [language catalog]
  (into (sorted-map)
        (keep (fn [[k message]]
                (try (MessageFormat. ^String message (ULocale. ^String language)) nil
                     (catch Exception e [k (ex-message e)]))))
        catalog))

(defn find-problems-in-catalogs
  "For an app's test, so catalogs stay complete before they ship: every
   key in every language. {} when there are none; else by language, the
   keys some other catalog has and this one lacks (:missing-keys) and the
   messages ICU can't read (:bad-messages, with why); a catalog that isn't
   there, under :missing-catalogs."
  [settings]
  (let [files    (list-catalog-files settings)
        read     (into {} (keep (fn [{:keys [language resource]}] (some->> (read-catalog resource) (vector language)))) files)
        all-keys (into #{} (mapcat keys) (vals read))
        missing  (vec (for [{:keys [language resource]} files :when (not (contains? read language))] resource))]
    (cond-> (into {}
                  (keep (fn [[language catalog]]
                          (let [lacks (sort (set/difference all-keys (set (keys catalog))))
                                bad   (find-bad-messages language catalog)]
                            (when (or (seq lacks) (seq bad))
                              [language (cond-> {}
                                          (seq lacks) (assoc :missing-keys (vec lacks))
                                          (seq bad)   (assoc :bad-messages bad))]))))
                  read)
      (seq missing) (assoc :missing-catalogs missing))))
