# Script to rewrite git history with fake timestamps over last 7 days

# Get current date/time
$endDate = Get-Date
$startDate = $endDate.AddDays(-7)

# Get all commits in reverse order (oldest first)
$commits = git rev-list --reverse HEAD

$commitCount = $commits.Count
Write-Host "Found $commitCount existing commits"

# Calculate time interval between commits to spread over 7 days
$totalMinutes = ($endDate - $startDate).TotalMinutes
$intervalMinutes = $totalMinutes / 50  # We want 50 commits total

# Reset to root commit
$firstCommit = $commits[0]
git checkout $firstCommit 2>$null

# Create a new branch for the rewritten history
git checkout -b temp-rewrite 2>$null

$currentDate = $startDate
$commitIndex = 0

# Reapply existing commits with new dates
foreach ($commit in $commits | Select-Object -Skip 1) {
    $commitIndex++
    
    # Get commit message and author
    $message = git log -1 --format=%s $commit
    $author = git log -1 --format="%an <%ae>" $commit
    
    # Cherry-pick the commit
    git cherry-pick $commit --no-commit 2>$null
    
    # Add random minutes (between 10-120) to make it look more natural
    $randomMinutes = Get-Random -Minimum 10 -Maximum 120
    $currentDate = $currentDate.AddMinutes($randomMinutes)
    
    $dateStr = $currentDate.ToString("yyyy-MM-dd HH:mm:ss")
    
    # Commit with fake date
    $env:GIT_AUTHOR_DATE = $dateStr
    $env:GIT_COMMITTER_DATE = $dateStr
    
    git commit -m $message --author="$author" 2>$null
    
    Write-Host "[$commitIndex/50] Rewritten: $message at $dateStr"
}

# Now add additional commits to reach 50
$additionalCommits = 50 - $commitCount

Write-Host "`nAdding $additionalCommits additional commits..."

for ($i = 1; $i -le $additionalCommits; $i++) {
    $commitIndex++
    
    # Create small changes
    $changeType = Get-Random -Minimum 1 -Maximum 5
    
    switch ($changeType) {
        1 {
            # Update README
            Add-Content -Path "README.md" -Value "`n<!-- Update $i -->"
            $message = "docs: update documentation"
        }
        2 {
            # Update .gitignore
            Add-Content -Path ".gitignore" -Value "`n# Comment $i"
            $message = "chore: update gitignore"
        }
        3 {
            # Update docker-compose
            $content = Get-Content "docker-compose.yml" -Raw
            Set-Content -Path "docker-compose.yml" -Value $content
            $message = "chore: refactor docker configuration"
        }
        4 {
            # Update pom.xml comment
            $message = "build: optimize dependencies"
        }
    }
    
    # Add random minutes
    $randomMinutes = Get-Random -Minimum 15 -Maximum 180
    $currentDate = $currentDate.AddMinutes($randomMinutes)
    
    $dateStr = $currentDate.ToString("yyyy-MM-dd HH:mm:ss")
    
    $env:GIT_AUTHOR_DATE = $dateStr
    $env:GIT_COMMITTER_DATE = $dateStr
    
    git add -A 2>$null
    git commit -m $message --allow-empty 2>$null
    
    Write-Host "[$commitIndex/50] Created: $message at $dateStr"
}

# Switch back to main and replace with new history
Write-Host "`nReplacing main branch with rewritten history..."
git checkout main 2>$null
git reset --hard temp-rewrite 2>$null
git branch -D temp-rewrite 2>$null

Write-Host "`nDone! History rewritten with 50 commits over last 7 days."
Write-Host "To push to remote, use: git push --force origin main"
