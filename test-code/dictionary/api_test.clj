(ns dictionary.api-test
  "The server's side: a request's language, by its prefix, the cookie,
   Accept-Language and the default; the alternate links; the settings
   checked."
  (:require
   [clojure.test :refer [deftest is testing]]
   [dictionary.api :as dictionary]))

(def ^:private settings
  (dictionary/check-settings {:languages ["en" "de" "hu"] :default "en" :cookie-name "lang"}))

(defn- make-request [uri headers]
  {:uri uri :headers headers})

(deftest the-names-of-the-api
  (is (= '#{settings-shape check-settings find-language-of-request make-alternate-links
            split-language-from-path make-path-in-language}
         (set (keys (ns-publics 'dictionary.api))))))

(deftest a-requests-language
  (testing "the address's prefix first, taken off the path"
    (is (= {:language "de" :path "/orders/7"}
           (dictionary/find-language-of-request settings (make-request "/de/orders/7" {"cookie" "lang=hu" "accept-language" "hu"}))))
    (is (= {:language "de" :path "/"} (dictionary/find-language-of-request settings (make-request "/de" {}))))
    (is (= {:language "en" :path "/orders"} (dictionary/find-language-of-request settings (make-request "/en/orders" {})))
        "the default's prefix, given anyway, is read as it"))
  (testing "then the cookie the settings name, when it holds one of the languages"
    (is (= "hu" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"cookie" "a=1; lang=hu" "accept-language" "de"})))))
    (is (= "de" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"cookie" "lang=fr" "accept-language" "de"}))))))
  (testing "then Accept-Language, most wanted first; a region falls back to its language"
    (is (= {:language "de" :path "/orders"}
           (dictionary/find-language-of-request settings (make-request "/orders" {"accept-language" "fr;q=0.9, de-AT;q=0.8, en;q=0.1"}))))
    (is (= "en" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"accept-language" "de;q=0, fr"}))))
        "quality 0 is not wanted"))
  (testing "then the default"
    (is (= "en" (:language (dictionary/find-language-of-request settings (make-request "/orders" {}))))))
  (testing "a first part that isn't a language stays in the path"
    (is (= {:language "en" :path "/deals"} (dictionary/find-language-of-request settings (make-request "/deals" {})))))
  (testing "no cookie named: it isn't read"
    (is (= "en" (:language (dictionary/find-language-of-request (assoc settings :cookie-name nil)
                                                                (make-request "/orders" {"cookie" "lang=hu"}))))))
  (testing "one language: every request is in it, and no prefix is taken"
    (let [one {:languages ["hu"] :default "hu" :cookie-name nil}]
      (is (= {:language "hu" :path "/de/orders"}
             (dictionary/find-language-of-request one (make-request "/de/orders" {"accept-language" "de"})))))))

(deftest alternate-links
  (is (= [{:hreflang "en" :href "https://shop.example/orders"}
          {:hreflang "de" :href "https://shop.example/de/orders"}
          {:hreflang "hu" :href "https://shop.example/hu/orders"}
          {:hreflang "x-default" :href "https://shop.example/orders"}]
         (dictionary/make-alternate-links settings "https://shop.example" "/orders")))
  (is (= ["https://shop.example/" "https://shop.example/de" "https://shop.example/hu" "https://shop.example/"]
         (map :href (dictionary/make-alternate-links settings "https://shop.example" "/")))
      "the home page")
  (is (= [] (dictionary/make-alternate-links {:languages ["hu"] :default "hu" :cookie-name nil} "https://x" "/"))
      "none with one language"))

(deftest the-settings-are-checked
  (let [errors (try (dictionary/check-settings {:languages ["en" "De"] :colour "red"})
                    (catch clojure.lang.ExceptionInfo e (:errors (ex-data e))))]
    (is (= #{:languages :default :cookie-name :colour} (set (keys errors))) "every problem at once"))
  (is (thrown? clojure.lang.ExceptionInfo
               (dictionary/check-settings {:languages ["en" "de"] :default "hu" :cookie-name nil}))
      "the default is one of the languages")
  (is (thrown? clojure.lang.ExceptionInfo
               (dictionary/check-settings {:languages ["en" "en"] :default "en" :cookie-name nil}))))
