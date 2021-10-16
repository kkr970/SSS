import numpy as np
import tensorflow as tf
import pandas as pd

model = tf.keras.models.load_model('isFall_Model_1.h5')
list_YES = [9, 10, 11, 10, 9, 9, 9, 8, 10, 11, 12, 10, 10, 9, 0, 0, 0, 13, 12, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9]
list_NO = [9, 10, 11, 10, 9, 9, 9, 8, 10, 11, 12, 10, 10, 9, 9, 9, 9, 13, 12, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9]
list_IFFY1 = [9, 10, 11, 10, 9, 9, 9, 8, 10, 11, 12, 10, 10, 9, 0, 0, 9, 13, 12, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9] #0이 2개 애매함
list_IFFY2 = [9, 10, 11, 10, 9, 9, 9, 8, 10, 11, 12, 10, 10, 9, 0, 9, 9, 13, 12, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9] #0이 1개  아님
list_IFFY3 = [9, 10, 11, 10, 9, 0, 9, 8, 10, 11, 12, 10, 10, 0, 9, 9, 9, 13, 12, 9, 9, 9, 9, 9, 9, 0, 9, 9, 9, 9] #0이 여러개 아님

predict1 = model.predict([list_YES, list_NO, list_IFFY1, list_IFFY2, list_IFFY3])
print(predict1)
