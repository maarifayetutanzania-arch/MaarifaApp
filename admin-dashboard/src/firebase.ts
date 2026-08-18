import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getFunctions } from "firebase/functions";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyCcNlowv61JgW0Bda9kkdFtLlWA8QZSIIw",
  authDomain: "maarifaapp-aa585.firebaseapp.com",
  projectId: "maarifaapp-aa585",
  storageBucket: "maarifaapp-aa585.firebasestorage.app",
  messagingSenderId: "106555594589",
  appId: "1:106555594589:web:10e80c6816d4f359ad3ab4"
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const functions = getFunctions(app);
export const storage = getStorage(app);+
