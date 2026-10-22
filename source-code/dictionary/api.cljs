(ns dictionary.api
  "The page's language, in the browser: read from the address, and kept
   when an address is made. The server's side is this namespace's .clj
   half, which picked the language the page came in (its prefix, the
   cookie, Accept-Language, the default).

     (dictionary/find-language-of-path settings \"/de/orders\" \"en\")   ;=> \"de\"
     (dictionary/find-language-of-path settings \"/orders\" \"de\")      ;=> \"de\": no prefix, the page's
     (dictionary/make-path-in-language settings \"/orders\" \"de\")      ;=> \"/de/orders\"

   The settings are the server's, carried in the page; check them once,
   check-settings, before using them."
  (:require
   [dictionary.addresses :as addresses]
   [dictionary.settings :as settings]))

(def settings-shape
  "The settings, every key required: {:languages [\"en\" \"de\"] :default
   \"en\" :cookie-name \"lang\"} (see dictionary.settings)."
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
