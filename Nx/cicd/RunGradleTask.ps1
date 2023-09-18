param ([Parameter(Mandatory)][string]$appdir,
       [Parameter(Mandatory)][string]$gradleTask,
       [string[]]$taskParameters=@())

Start-Transcript Transcript.log
"Starting Unit Run Task" | Out-File -FilePath "$appdir\Task.log"
"Running $gradleTask with parameters [$taskParameters] in the path $appdir"
cd $appdir
"In Workspace Directory" | Out-File -Append -FilePath "$appdir\Task.log"
dir | Out-File -Append -FilePath "$appdir\Task.log"

$gradleArgs = @()
if ($taskParameters.Count) {
    foreach ($parameter in $taskParameters) {
        $gradleArgs += "-P" + $parameter
    }
}
"Executing Command './gradlew --console=plain tasks'" | Out-File -Append -FilePath "$appdir\Task.log"

./gradlew $gradleTask $gradleArgs  | Out-File -Append -FilePath "$appdir\Task.log"