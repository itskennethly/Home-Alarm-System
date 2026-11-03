clear;
% Initialize Arduino
myBoard = arduino('COM4', 'Nano3');
% Pin Assignments
motionPin = 'D8';      % Motion sensor connected to pin D8
disalarmPin = 'D6';    % Disarm button connected to pin D6
soundPin = 'D5';       % Buzzer connected to pin D5
lightPin = 'D4';       % Light connected to pin D4
% Variables
buzzerCount = 0;       % Counter for the number of times the buzzer has triggered
alarmActive = false;    % Initially, the alarm is ON
disp('Starting Alarm System...');
% Main Loop
while true
   % Check the disarm button
   button = readDigitalPin(myBoard, disalarmPin);
   if button == 1
       alarmActive = toggleAlarmState(myBoard, disalarmPin, lightPin, alarmActive);
   end
   % If the alarm is active, check for motion
   if alarmActive
       motionSensor = readDigitalPin(myBoard, motionPin);
       if motionSensor == 0
           buzzerCount = handleMotion(myBoard, soundPin, lightPin, buzzerCount);
       else
           disp('No Motion Detected');
       end
   end
   pause(0.1);  % Short delay for loop stability
end
% Function to toggle alarm state
function alarmActive = toggleAlarmState(myBoard, disalarmPin, lightPin, alarmActive)
   alarmActive = ~alarmActive;  % Toggle alarm state
   if alarmActive
       disp('Alarm is now ON');
       writeDigitalPin(myBoard, lightPin, 1);  % Turn on the light
   else
       disp('Alarm is now OFF');
       writeDigitalPin(myBoard, lightPin, 0);  % Turn off the light
   end
  
   % Wait for button release to avoid multiple toggles
   while readDigitalPin(myBoard, disalarmPin) == 1
       pause(0.1);
   end
end
% Function to handle motion detection
function buzzerCount = handleMotion(myBoard, soundPin, lightPin, buzzerCount)
   disp('Motion Detected');
   if buzzerCount < 1
       % Flash light and beep the buzzer
       for i = 1:1
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.2); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 0);  % Turn off light
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.2); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 1);  % Turn on light
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.2); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 0);  % Turn off light
            pause(0.4);  % Short pause
            writeDigitalPin(myBoard, lightPin, 1);  % Turn on light
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.4); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 0);  % Turn off light
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.4); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 1);  % Turn on light
            writePWMDutyCycle(myBoard, soundPin, 0.35); pause(0.4); writeDigitalPin(myBoard, soundPin, 0); pause(0.2);  % Beep
            writeDigitalPin(myBoard, lightPin, 0);  % Turn off light
            pause(0.4);  % Short pause
            writeDigitalPin(myBoard, lightPin, 1);  % Turn on light
       end
       buzzerCount = buzzerCount + 1;
   else
       disp('Alarm Stopped');
       buzzerCount = 0;  % Reset buzzer count
       writeDigitalPin(myBoard, lightPin, 0);  % Turn off light
   end
end
