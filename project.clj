(defproject com.ingemark/lein-upload "0.1.1-SNAPSHOT"
  :description "Upload a file to a repository"
  :url "https://github.com/Inge-mark/bundle-pbxis-ws"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in :leiningen
  :lein-release {:deploy-via :clojars}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [s3-wagon-private "1.1.2"]])

