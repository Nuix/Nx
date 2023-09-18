param ([Parameter(Mandatory)][string]$appdir,
       [Parameter(Mandatory)][string]$gradleTask,
       [string[]]$taskParameters=@())

Start-Transcript Transcript.log
"Starting Unit Run Task" | Out-File -FilePath "$appdir\Task.log"
"Running $gradleTask with parameters [$taskParameters] in the path $appdir"
cd "$appdir\Nx"
"In Workspace Directory" | Out-File -Append -FilePath "$appdir\Task.log"
dir | Out-File -Append -FilePath "$appdir\Task.log"

$gradleCommand = 'gradlew'
if ($taskParameters.Count) {
    for ($parameter in $taskParameters) {
        $gradleCommand = $gradleCommand + " -P" + $parameter
    }
}
$gradleCommand = $gradleCommand + " " + $gradleTask
"Executing Command '$gradleCommand'" | Out-File -Append -FilePath "$appdir\Task.log"

&$gradleCommand | Out-File -Append -FilePath "$appdir\Task.log"