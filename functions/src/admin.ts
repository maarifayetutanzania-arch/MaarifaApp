import * as admin from "firebase-admin";

if (admin.apps.length === 0) {
  admin.initializeApp();
}

export const db = admin.firestore();
export const messaging = admin.messaging();
export const FieldValue = admin.firestore.FieldValue;
export const Timestamp = admin.firestore.Timestamp;

export const COLLECTIONS = {
  USERS: "users",
  TEACHERS: "teachers",
  MATERIALS: "materials",
  SUBSCRIPTIONS: "subscriptions",
  DOWNLOADS: "downloads",
  ENGAGEMENT: "engagement",
  PAYOUTS: "payouts",
  NOTIFICATIONS: "notifications",
} as const;

// Mirrors PRD 14 / app/.../util/Constants.kt AppConfig — keep both in sync.
export const TEACHER_POOL_SHARE = 0.75;
export const PLATFORM_SHARE = 0.25;

export const PLAN_DURATIONS_DAYS: Record<string, number> = {
  WEEKLY: 7,
  MONTHLY: 30,
  QUARTERLY: 90,
};

export const PLAN_AMOUNTS_TZS: Record<string, number> = {
  WEEKLY: 3000,
  MONTHLY: 10000,
  QUARTERLY: 25000,
};
