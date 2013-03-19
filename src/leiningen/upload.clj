(ns leiningen.upload
  (require (leiningen [deploy :refer [repo-for]])
           [leiningen.core.main :as main]
           (clojure.java [shell :as sh] [io :as io])
           [clojure.string :as s]
           [cemerick.pomegranate.aether :as aether])
  (import cemerick.pomegranate.aether.PomegranateWagonProvider
          org.apache.maven.wagon.repository.Repository
          org.apache.maven.wagon.authentication.AuthenticationInfo))

(set! *warn-on-reflection* true)

(defn abort [fmt & args] (main/abort (apply format fmt args)))

(defn sh! [& args]
  (apply println "$" args)
  (let [res (apply sh/sh args)]
    (print (:out res)) (print (:err res)) (flush)
    (when-not (zero? (:exit res)) (abort "Command failed with exit code %s: %s" (:exit res) args))))

(defn upload [project filename reponame repodir]
  (let [repo (second (leiningen.deploy/repo-for project reponame))
        repo-obj (Repository. "upload" (:url repo))
        f (io/file filename)]
    (println "Upload" filename "==>" (:url repo))
    (doto (.lookup (PomegranateWagonProvider.) (.getProtocol repo-obj))
      (.connect repo-obj (doto (AuthenticationInfo.)
                           (.setUserName (:username repo))
                           (.setPassword (:password repo))
                           (.setPassphrase (:passphrase repo))))
      (.put f (format "%s/%s" repodir (.getName f))))))

#_(let [files {[(symbol (:group project) (:name project)) (:version project) :extension "tgz"]
               tarfile}]
    (aether/deploy-artifacts
     :artifacts (keys files)
     :files files
     :transfer-listener :stdout
     :repository [(leiningen.deploy/repo-for project "releases")]))
