(ns dictionary.settings
  "dictionary's settings, as a malli shape, checked before anything is
   read. Nothing has a default: every key is written out, nil where it is
   a choice, and a missing or unknown one refuses, named, with every other
   problem at once (:dictionary/bad-settings)."
  (:require
   [malli.core :as malli]
   [malli.error :as malli-error]))

(def ^:private language-tag
  [:re {:error/message "should be a language tag in lowercase: \"en\", \"de\", \"pt-br\""}
   #"^[a-z]{2,3}(-[a-z0-9]{2,8})*$"])

(defn- is-default-one-of-the-languages? [{:keys [languages default]}]
  (or (not (vector? languages)) (not (string? default)) (some #{default} languages)))

(defn- are-languages-distinct? [{:keys [languages]}]
  (or (not (vector? languages)) (= (count languages) (count (distinct languages)))))

(def settings-shape
  "The settings, every key required.
   :languages: the app's languages, as lowercase tags, [\"en\" \"de\"].
   :default: one of them: the language whose addresses have no prefix
   (/orders, while German's are /de/orders), and the one a request is in
   when nothing else says.
   :cookie-name: the cookie a person's chosen language is read from (the
   app writes it), or nil for none."
  [:and
   [:map {:closed true}
    [:languages [:vector {:min 1} language-tag]]
    [:default language-tag]
    [:cookie-name [:maybe [:string {:min 1}]]]]
   [:fn {:error/message ":default is one of :languages"} is-default-one-of-the-languages?]
   [:fn {:error/message ":languages are each named once"} are-languages-distinct?]])

(defn check-settings
  "Returns the settings; throws {:type :dictionary/bad-settings :errors …}
   with every problem at once when they don't fit."
  [settings]
  (when-let [explanation (malli/explain settings-shape settings)]
    (let [errors (malli-error/humanize explanation)]
      (throw (ex-info (str "The settings have problems: " (pr-str errors))
                      {:type :dictionary/bad-settings :errors errors}))))
  settings)
