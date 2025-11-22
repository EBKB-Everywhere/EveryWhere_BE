# ~~~~~~~~~~~audio to features~~~~~~~~~~~~
# -----------------------------
# 1. 밴드 에너지 계산용 보조 함수
# -----------------------------
def band_energy(signal, sr, low, high):
    fft = np.abs(np.fft.rfft(signal))
    freqs = np.fft.rfftfreq(len(signal), d=1.0/sr)
    idx = np.where((freqs >= low) & (freqs <= high))[0]
    return fft[idx].mean() if len(idx) > 0 else 0


# -----------------------------
# 2. SPL 계산
# -----------------------------
def calc_spl(signal):
    rms = np.sqrt(np.mean(signal ** 2))
    return 20 * np.log10(rms + 1e-7)


# -----------------------------
# 3. MFCC + 잡음 보정
# -----------------------------
def extract_mfcc(signal, sr, n_mfcc=20):
    mfcc = librosa.feature.mfcc(y=signal, sr=sr, n_mfcc=n_mfcc)
    return mfcc.mean(axis=1), mfcc.var(axis=1)


# -----------------------------
# 4. 전체 오디오 특징 추출
# -----------------------------
def extract_audio_features(path=r"C:\realthon_t6\vid1.wav", n_mfcc=20):
    signal, sr = librosa.load(path, sr=None)

    # 1) SPL
    spl = calc_spl(signal)

    # 2) MFCC mean + var
    mfcc_mean, mfcc_var = extract_mfcc(signal, sr, n_mfcc=n_mfcc)

    # 3) ZCR
    zcr = librosa.feature.zero_crossing_rate(y=signal).mean()

    # 4) Centroid
    centroid = librosa.feature.spectral_centroid(y=signal, sr=sr).mean()

    # 5) Band energies
    band0_300 = band_energy(signal, sr, 0, 300)
    band300_3000 = band_energy(signal, sr, 300, 3000)
    band3000_8000 = band_energy(signal, sr, 3000, 8000)

    band_ratio_speech = band300_3000 / (band0_300 + 1e-7)

    # 모든 feature 평탄화해서 하나의 벡터로 합침
    features = {
        "spl": spl,
        "zcr": zcr,
        "centroid": centroid,
        "band0_300": band0_300,
        "band300_3000": band300_3000,
        "band3000_8000": band3000_8000,
        "speech_noise_ratio": band_ratio_speech,
    }

    # MFCC 추가
    for i, v in enumerate(mfcc_mean):
        features[f"mfcc_{i}_mean"] = v
    for i, v in enumerate(mfcc_var):
        features[f"mfcc_{i}_var"] = v

    return features 


# ~~~~~~~~~~~image에서 사람 수 count~~~~~~~~~~~~~~
# 사람 수 감지 함수
def count_people(image_path):
    # YOLOv8 모델 로드
    model = YOLO("yolov8n.pt")
    img = cv2.imread(image_path)

    if img is None:
        print(f"[WARNING] Cannot read: {image_path}")
        return 0

    results = model(img, verbose=False)
    boxes = results[0].boxes
    
    person_count = 0

    for box in boxes:
        cls = int(box.cls)
        if cls == 0:  # YOLO의 person 클래스 ID = 0
            person_count += 1

    return person_count

# ~~~~~~~~~~~feature dict~~~~~~~~~~
def build_features(img_path, ble_raw, audio_path):
    img_count = count_people(img_path)
    ble_feats = ble_raw
    audio_feats = extract_audio_features(audio_path)

    row = {
        "numberOfHuman": img_count,
        "bleNum": ble_feats,
        **audio_feats
    }
    return row 

# ~~~~~~~~~~~~~~~~~predict(이거돌리는거임)~~~~~~~~~~~~~~~~

def predict_crowd(img_path, ble_raw, audio_path, ID):
    
    model = joblib.load("crowd_classifier.pkl")
    
    top_features = [
        'mfcc_9_mean', 'mfcc_7_mean', 'zcr', 'band0_300',
        'numberOfHuman', 'speech_noise_ratio', 'mfcc_3_mean',
        'mfcc_14_mean', 'mfcc_8_mean', 'centroid', 'bleNum'
    ]
    
    """
    feature_dict 예시:
    {
       "mfcc_9_mean": -132.1,
       "mfcc_7_mean": 22.3,
       "zcr": 0.01,
       "band0_300": 47.1,
       "numberOfHuman": 14,
       "speech_noise_ratio": 0.22,
       "mfcc_3_mean": 30.4,
       "mfcc_14_mean": 4.12,
       "mfcc_8_mean": -2.11,
       "centroid": 1750.2,
       "bleNum": 83
    }
    """
    feature_dict = build_features(img_path, ble_raw, audio_path)
    row = {f: feature_dict[f] for f in top_features}

    df = pd.DataFrame([row])
    pred = model.predict(df)[0]            # class 0/1/2
    prob = model.predict_proba(df)[0]      # softmax 확률

    if pred == 0:
        result = round(6+random.uniform(-6, 6))
    elif pred == 1:
        result = round(19+random.uniform(-7, 7))
    else:
        result = round(32+random.uniform(-6, 6))
        
    return ID, result