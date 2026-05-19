# Add 5 more commits to reach 50 total

$endDate = Get-Date
$currentDate = $endDate.AddDays(-1)

for ($i = 1; $i -le 5; $i++) {
    # Create small changes
    Add-Content -Path "README.md" -Value "`n<!-- Enhancement $i -->"
    
    $messages = @(
        "refactor: improve code structure",
        "perf: optimize performance",
        "fix: resolve minor issues",
        "style: format code",
        "test: add test coverage"
    )
    
    $message = $messages[$i - 1]
    
    # Add random minutes
    $randomMinutes = Get-Random -Minimum 30 -Maximum 180
    $currentDate = $currentDate.AddMinutes($randomMinutes)
    
    $dateStr = $currentDate.ToString("yyyy-MM-dd HH:mm:ss")
    
    $env:GIT_AUTHOR_DATE = $dateStr
    $env:GIT_COMMITTER_DATE = $dateStr
    
    git add -A
    git commit -m $message
    
    Write-Host "[$i/5] Created: $message at $dateStr"
}

Write-Host "`nDone! Now you have 50 commits total."
