param([string]$Language='en-US')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Speech
$installed=@([System.Speech.Recognition.SpeechRecognitionEngine]::InstalledRecognizers())
if($installed.Count -eq 0){throw 'No offline Windows speech recognizer is installed'}
$selected=$installed | Where-Object {$_.Culture.Name -eq $Language} | Select-Object -First 1
if(!$selected){$selected=$installed[0]}
$recognizer=New-Object System.Speech.Recognition.SpeechRecognitionEngine -ArgumentList $selected.Id
$recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar))
$recognizer.SetInputToDefaultAudioDevice()
$subscription=Register-ObjectEvent -InputObject $recognizer -EventName SpeechRecognized -SourceIdentifier SplitSelfSpeech -Action {
 $text=$Event.SourceEventArgs.Result.Text
 if($text -and $text.Length -le 512){[Console]::WriteLine([Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($text)));[Console]::Out.Flush()}
}
try{$recognizer.RecognizeAsync([System.Speech.Recognition.RecognizeMode]::Multiple);while($true){Wait-Event -Timeout 1 | Out-Null}}
finally{$recognizer.RecognizeAsyncCancel();Unregister-Event -SourceIdentifier SplitSelfSpeech -ErrorAction SilentlyContinue;$recognizer.Dispose()}
