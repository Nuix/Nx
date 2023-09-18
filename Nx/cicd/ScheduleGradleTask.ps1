# Unit tests fail on the WinRM client - WinRM environment does not provide a good environment for
# doing the encryption, which is needed to store credentials used for get license details.  As a workaround, push
# Unit testing to a scheduled task so it runs in a more normal environment.

param ([Parameter(Mandatory)][string]$appdir,
       [Parameter(Mandatory)][string]$gradleTask,
       [string[]]$taskParameters=@())


$start_time = (Get-Date).AddMinutes(1).ToString("HH:mm")
$ready_state_timeout = (Get-Date).AddMinutes(5)

$task_name = "nx_" + $gradleTask
$powershell_command = "$Env:windir\System32\WindowsPowerShell\v1.0\powershell.exe"
$taskParameterString = "@(" + ($taskParameters -Join ", ") + ")"
$taskParameterString = $taskParameterString + ")
$powershell_argument = "-NoProfile -NonInteractive -ExecutionPolicy Bypass -File $appdir\cicd\RunGradleTask.ps1 -appdir $appdir -gradleTask $gradleTask -taskParameters $taskParameterString"
$task_action = New-ScheduledTaskAction -Execute $powershell_command `
                                       -WorkingDirectory $appdir `
                                       -Argument $powershell_argument
Write-Host "Action: " $task_action.Execute $task_action.WorkingDirectory $task_action.Arguments

$task_trigger = New-ScheduledTaskTrigger -Daily -At 1am
$task_settings = New-ScheduledTaskSettingsSet -StartWhenAvailable `
                                              -AllowStartIfOnBatteries `
                                              -DontStopIfGoingOnBatteries `
                                              -Priority 4
Register-ScheduledTask -TaskName $task_name -Trigger $task_trigger `
                       -RunLevel Highest -Settings $task_settings -Force `
                       -Action $task_action `
                       -User system

$task_started = $false
$task_complete = $false
$task_timeout = $false
$timeout_cause = $null
$run_state_timeout = $null

Start-ScheduledTask -TaskName $task_name
For(; -Not $task_complete; ) {
    $task_obj = Get-ScheduledTask -TaskName $task_name
    $task_state = $task_obj.State
    If(-Not $task_started) {
        $task_started = "Running" -eq $task_state
        If(-Not $task_started -and $ready_state_timeout -lt (Get-Date)) {
            $task_complete = $true
            $task_timeout = $true
            $timeout_cause = "Timeout waiting for task to start"
        } else {
            $run_state_timeout = (Get-Date).AddMinutes(30)
        }
    } else {
        $task_complete = "Running" -ne $task_state
        If(-Not $task_complete -and $run_state_timeout -lt (Get-Date)) {
            $task_complete = $true
            $task_timeout = $true
            $timeout_cause = "Timeout waiting for the task to finish running."
        }
    }
    If(-Not $task_complete) {
        Write-Host "$task_name - $task_state.  Waiting 10s"
        Start-Sleep -Seconds 10
    }
}

$task_info = Get-ScheduledTaskInfo -TaskName $task_name
Unregister-ScheduledTask -TaskName $task_name -Confirm:$false

Write-Host "$gradleTask Ran At:" $task_info.LastRunTime
Write-Host "$gradleTask Last Results:" $task_info.LastTaskResult

If($task_timeout) {
    Write-Host "$gradleTask Runner Timed Out: $timeout_cause"
    Exit 3
} Else {
    Write-Host "$gradleTask Runner Completed. $LASTEXITCODE"
    Exit $task_info.LastTaskResult
}