#EYEDETECTION_FACE CV

#importing Libraries  
import cv2

EYEDETECTION_PATH = 'Facedetection/haarcascade_eye.xml'  #Giving Path of the file
EYE_DETECTION = cv2.CascadeClassifier(EYEDETECTION_PATH)
cam = cv2.VideoCapture(0)
while(True):
    #Reading the webcam
    _, frame = cam.read()

    #Converting to Grayscale img
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)  #Gray Scale Frame 

    #Returns coordinates of eyes in the faces of the frame 
    eye = EYE_DETECTION.detectMultiScale(gray, 1.3, 5)

    #Cycle through each coordinate list 
    for eye_dims in eye:

        #Desctructing data and extracted bounding box coordinates
        (x,y,w,h) = eye_dims


        #Drawing -"Bounding Box"
        frame = cv2.rectangle(frame, (x,y), (x+h, y+h), (0,255,255), 2)
        frame = cv2.putText(frame, str(x), (x,y), cv2.FONT_HERSHEY_DUPLEX, 0.7,(0,0,255), 2) 





    #Displaying -"Bounding Box"
    cv2.imshow('My_Frame', frame)
    key = cv2.waitKey(1) #Capturing The image
    if(key == 27):                   
        break

cam.release()
cv2.destroyAllWindows()
