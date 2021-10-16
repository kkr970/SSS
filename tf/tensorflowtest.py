import pandas as pd
import numpy as np
import tensorflow as tf

#학습시킬 데이터 만들기, x, y 데이터 생성
xdata = []
data_y = pd.read_csv('C:/Users/user/Desktop/tf/data/yData.csv')
ydata = data_y['answer'].values
temp = []

for i in range(1, 1001):#1001
    data = pd.read_csv("C:/Users/user/Desktop/tf/data/fall/001~1001/Data"+ format(i, '03') +".csv")
    for i, rows in data.iterrows():
        num = (rows['accX']**2 + rows['accY']**2 + rows['accZ']**2)**(1/2)
        num = num.item()
        temp.append( num )
        if(i == 30): break;
    xdata.append( temp )
    temp = []



for i in range(1, 553):#553
    data = pd.read_csv("C:/Users/user/Desktop/tf/data/nofall/Data"+ format(i, '03') +".csv")
    for i, rows in data.iterrows():
        num = (rows['accX']**2 + rows['accY']**2 + rows['accZ']**2)**(1/2)
        num = num.item()
        temp.append( num )
        if(i == 30): break;
    xdata.append( temp )
    temp = []

#실험으로 파악해야함 dense 내부 숫자는 , 마지막꺼는 1개의 노드가 될것
#레이어를 추가, 제거, 숫자 변경, 함수변경 등 확률증가를 위해 이것저것 할 수 있음
#activation = 'relu sigmoid tanh softmax leakyRelu' 마지막에는 sigmoid를 넣어서 0~1로 확률계산하는게 좋음
model = tf.keras.models.Sequential([
    tf.keras.layers.Dense(64, activation='sigmoid'),
    tf.keras.layers.Dense(128, activation='sigmoid'),
    tf.keras.layers.Dense(1, activation='sigmoid'),
])

#손실 함수중에 binary_crossentropy가 분류, 확률에 많이 사용함
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

#epochs = 전체 자료를 몇번 반복학습하는가?
#일반 파이썬 리스트로 안들어감, np.array로 변경해서 넣자
model.fit( np.array(xdata), np.array(ydata), epochs=100 )

#모델 요약해서 보여주기
model.summary()

#학습한 모델 저장하기, 불러오기는 model2 = tf.keras.models.load_model('경로')
model.save('isFall_Model_1.h5')
