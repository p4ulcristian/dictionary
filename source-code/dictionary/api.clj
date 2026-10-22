(ns dictionary.api
  "The request's language, and the page's addresses in every language: the
   server's side. The browser's is this namespace's .cljs half.

     (def settings (dictionary/check-settings {:languages   [\"en\" \"de\"]
                                               :default     \"en\"      ; its addresses carry no prefix
                                               :cookie-name \"lang\"})) ; or nil

     (dictionary/find-language-of-request settings request)
     ;=> {:language \"de\" :path \"/orders\"}   ; for /de/orders, or /orders with a German browser
     (dictionary/make-alternate-links settings \"https://shop.example\" \"/orders\")
     ;=> [{:hreflang \"en\" :href \"https://shop.example/orders\"}
     ;    {:hreflang \"de\" :href \"https://shop.example/de/orders\"}
     ;    {:hreflang \"x-default\" :href \"https://shop.example/orders\"}]
     (dictionary/make-path-in-language settings \"/orders\" \"de\")   ;=> \"/de/orders\"

   A request's language: the address's prefix (with more than one
   language), then the cookie the settings name, then Accept-Language, then
   the default. dictionary picks the language; the page's texts in it come
   later. The settings are checked once, by check-settings, before the app
   starts; the functions trust them."
  (:require
   [dictionary.addresses :as addresses]
   [dictionary.requests :as requests]
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

(defn find-language-of-request
  "{:language … :path …}: the request's language, and its path (:uri)
   without the language's prefix, for routing."
  [settings request]
  (requests/find-language-of-request settings request))

(defn make-alternate-links
  "The page at path (no language in it) in every language, and x-default,
   the default's, each {:hreflang :href} with the origin in front; [] with
   one language."
  [settings origin path]
  (requests/make-alternate-links settings origin path))

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
