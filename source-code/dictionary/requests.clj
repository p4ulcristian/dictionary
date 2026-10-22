(ns dictionary.requests
  "A request's language, on the server: the address's prefix (with more
   than one language), then the cookie (when the settings name one and it
   holds one of the languages), then the browser's Accept-Language (a tag,
   or its first part: de-AT is de), then the default. With one language,
   every request is in it."
  (:require
   [clojure.string :as str]
   [dictionary.addresses :as addresses]))

(defn- read-cookie [request cookie-name]
  (some (fn [pair]
          (let [[k v] (str/split (str/trim pair) #"=" 2)]
            (when (= k cookie-name) v)))
        (some-> (get-in request [:headers "cookie"]) (str/split #";"))))

(defn- read-quality [params]
  (or (some #(some-> (re-matches #"q=([0-9.]+)" %) second parse-double) params) 1.0))

(defn list-accepted-languages
  "Accept-Language's tags, lowercase, most wanted first; a tag of quality
   0, and *, left out."
  [header]
  (->> (str/split (or header "") #",")
       (keep (fn [entry]
               (let [[tag & params] (map str/trim (str/split entry #";"))
                     quality        (read-quality params)]
                 (when (and (seq tag) (not= "*" tag) (pos? quality))
                   [(str/lower-case tag) quality]))))
       (sort-by (comp - second))
       (map first)))

(defn- find-accepted-language [{:keys [languages]} header]
  (let [ours (set languages)]
    (some (fn [tag] (or (ours tag) (ours (first (str/split tag #"-")))))
          (list-accepted-languages header))))

(defn find-language-of-request
  "{:language \"de\" :path \"/orders\"}: the request's language, and its
   path (:uri) without the language's prefix."
  [{:keys [default cookie-name languages] :as settings} request]
  (let [{:keys [language path]} (addresses/split-language-from-path settings (:uri request))]
    {:language (or language
                   (when (= 1 (count languages)) default)
                   (when cookie-name ((set languages) (read-cookie request cookie-name)))
                   (find-accepted-language settings (get-in request [:headers "accept-language"]))
                   default)
     :path     path}))

(defn make-alternate-links
  "The same page in every language, for <link rel=\"alternate\"
   hreflang>: [{:hreflang \"en\" :href \"https://shop.example/orders\"}
   {:hreflang \"de\" :href \"https://shop.example/de/orders\"} …
   {:hreflang \"x-default\" :href the default's}]; none with one
   language. path carries no language; origin is the site's,
   \"https://shop.example\"."
  [{:keys [languages default] :as settings} origin path]
  (if (< 1 (count languages))
    (conj (mapv (fn [language] {:hreflang language :href (str origin (addresses/make-path-in-language settings path language))})
                languages)
          {:hreflang "x-default" :href (str origin (addresses/make-path-in-language settings path default))})
    []))
