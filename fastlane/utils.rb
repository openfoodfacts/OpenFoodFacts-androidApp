require "versionomy"

def play_store_version_code_to_upload_to()
  puts "fetching play_store_version_code_to_upload_to"
  versionCode = ["internal", "alpha", "beta", "production"].map { |branch|
    begin
      versionCode = google_play_track_version_codes(track: branch).max
      versionCode ||= 1
    rescue
      1
    end
  }
  return versionCode.max + 1
end

def play_store_version_to_upload_to(minimumVersionName: "0.0.1")
  puts "fetching play_store_version_to_upload_to, min is #{minimumVersionName}"

  versionParsed = Versionomy.parse(minimumVersionName)

  ["internal", "alpha", "beta", "production"].each { |branch|
    trackVersionName = google_play_track_version_name(track: branch)
    unless trackVersionName.nil?
      trackVersionNameParsed = Versionomy.parse(trackVersionName)
      if branch == "production" && trackVersionNameParsed >= versionParsed
        versionParsed = trackVersionNameParsed.bump(:tiny)
      elsif trackVersionNameParsed > versionParsed
        versionParsed = trackVersionNameParsed
      end
    end
  }
  return versionParsed.to_s
end

def version_to_use_from_git_branch(gitBranch:)
  if gitBranch =~ /release\/[0-9]+\.[0-9]+\.[0-9]+/
    gitBranchVersion = gitBranch.split("release/")[1]
    gitBranchVersionParsed = Versionomy.parse(gitBranchVersion)
    puts "Using version name #{gitBranchVersionParsed} (from current branch name #{gitBranch})"
    return gitBranchVersionParsed
  end
  FastlaneCore::UI.user_error!("ERROR: we are not on a release/x.y.z branch, no version could be inferred.")
end
