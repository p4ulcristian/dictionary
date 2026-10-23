(ns dictionary.api
  "The page's language, in the browser: read from the address, and kept
   when an address is made. The server's side is this namespace's .clj
   half, which picked the language the page came in (its prefix, the
   cookie, Accept-Language, the default).

     (dictionary/find-language-of-path settings \"/de/orders\" \"en\")   ;=> \"de\"
     (dictionary/find-language-of-path settings \"/orders\" \"de\")      ;=> \"de\": no prefix, the page's
     (dictionary/make-path-in-language settings \"/orders\" \"de\")      ;=> \"/de/orders\"
     (dictionary/list-languages-to-pick settings)   ;=> [{:code \"en\" :name \"English\"} …], a language menu's

     ;; the texts: the catalogs fetched from the server, the same files it reads
     (-> (dictionary/fetch-catalog! settings \"de\") (.then (fn [catalog] …)))
     (dictionary/make-text {:fallback-language \"en\" :catalogs {\"de\" de \"en\" en}}
                           \"de\" :orders.cart/add {:count 3})

   The settings are the server's, carried in the page; check them once,
   check-settings, before using them."
  (:require
   [dictionary.addresses :as addresses]
   [dictionary.catalogs :as catalogs]
   [dictionary.settings :as settings]))

(def settings-shape
  "The settings, every key required: {:languages [{:code \"en\" :name
   \"English\"} {:code \"de\" :name \"Deutsch\"}] :default \"en\"
   :cookie-name \"lang\" …} (see dictionary.settings)."
  settings/settings-shape)

(defn check-settings
  "Returns the settings; throws {:type :dictionary/bad-settings :errors …}
   with every problem at once when they don't fit."
  [settings]
  (settings/check-settings settings))

(defn find-language-of-path
  "The language an address's path is in: its prefix's, else
   page-language, the one the server picked for the page (an address
   without a prefix is in the visit's language)."
  [settings path page-language]
  (or (:language (addresses/split-language-from-path settings path)) page-language))

(defn split-language-from-path
  "{:language \"de\" :path \"/orders\"} for /de/orders; {:language nil
   :path path} when the path starts with none (always, with one language)."
  [settings path]
  (addresses/split-language-from-path settings path))

(defn make-path-in-language
  "A path (no language in it) in a language: /de/orders; the default's
   carries none, /orders."
  [settings path language]
  (addresses/make-path-in-language settings path language))

(defn list-live-languages
  "The tags of the languages that show here: the launched :languages,
   then the :preview-languages this environment shows."
  [settings]
  (settings/list-live-languages settings))

(defn list-languages-to-pick
  "What a language menu lists: the languages that show here, in the
   settings' order, each its tag and its name in itself, [{:code \"en\"
   :name \"English\"} {:code \"de\" :name \"Deutsch\"} …]."
  [settings]
  (settings/list-languages-to-pick settings))

(defn fetch-catalog!
  "A promise of a language's catalog, {\"orders.cart/add\" \"…\"}, from
   /<catalogs-path>/<language>.json on the page's origin; failed when the
   server has none."
  [settings language]
  (catalogs/fetch-catalog! settings language))

(defn make-text
  "The key's ICU message in the language, args filled in ({} for none),
   by intl-messageformat; the fallback language's when the language's
   catalog lacks it; the key's name when neither has it. texts:
   {:fallback-language … :catalogs {language catalog}}, the catalogs
   fetched so far."
  [texts language message-key args]
  (catalogs/make-text texts language message-key args))
