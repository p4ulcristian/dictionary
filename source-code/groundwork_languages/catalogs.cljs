(ns groundwork-languages.catalogs
  "The catalogs in the browser: fetched from the page's own origin, each
   language's /<catalogs-path>/<language>.json (the same file the server
   reads), and a message made into text by intl-messageformat (the app's
   package.json lists it)."
  (:require
   ["intl-messageformat" :refer [IntlMessageFormat]]
   [groundwork-languages.texts :as texts]))

(defn fetch-catalog!
  "A promise of a language's catalog, {\"orders.cart/add\" \"…\"}, fetched
   from /<catalogs-path>/<language>.json; failed when it couldn't be."
  [{:keys [catalogs-path]} language]
  (-> (js/fetch (str "/" catalogs-path "/" language ".json")
                #js {:method "GET" :mode "same-origin" :credentials "same-origin" :cache "no-cache"})
      (.then (fn [response]
               (if (.-ok response)
                 (.json response)
                 (throw (ex-info (str "No catalog for " language ": the server answered " (.-status response))
                                 {:type :groundwork-languages/missing-catalog :language language})))))
      (.then #(js->clj %))))

(defonce ^:private formats
  ;; each message's formatter, made once, by [language message]
  (atom {}))

(defn- make-format [language message]
  (or (get @formats [language message])
      ;; ignoreTag: a < in a message (\"Small (<30cm)\") is text, as ICU4J
      ;; reads it on the server; without it intl-messageformat reads a tag
      ;; and throws
      (let [made (IntlMessageFormat. message language nil #js {:ignoreTag true})]
        (swap! formats assoc [language message] made)
        made)))

(defn make-text
  "The key's message in the language, its ICU arguments filled in from
   args ({:count 3}, {} for none); in the fallback language when the
   language's catalog lacks it; the key's name when neither has it."
  [texts language message-key args]
  (if-let [{:keys [message] found-in :language} (texts/find-message texts language message-key)]
    (.format (make-format found-in message) (clj->js (texts/turn-args-into-names args)))
    (texts/turn-key-into-catalog-key message-key)))
