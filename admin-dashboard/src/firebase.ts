import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getFunctions } from "firebase/functions";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyCcNlowv61JgW0Bda9kkdFtLlWA8QZSIIw",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "maarifaapp-aa585.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "maarifaapp-aa585",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "maarifaapp-aa585.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "106555594589",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:106555594589:web:10e80c6816d4f359ad3ab4",
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const functions = getFunctions(app);
export const storage = getStorage(app);
