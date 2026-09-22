import { useEffect, useState } from "react";
import { Query, onSnapshot, DocumentData } from "firebase/firestore";

export function useCollection<T>(query: Query<DocumentData> | null) {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!query) {
      setData([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    const unsubscribe = onSnapshot(
      query,
      (snap) => {
        const docsData = snap.docs.map((d) => ({
          id: d.id, // Inahakikisha Firestore Document ID inajumuishwa mara zote
          ...(d.data() as T),
        }));
        setData(docsData);
        setLoading(false);
        setError(null);
      },
      (err) => {
        console.error("Firestore useCollection Error:", err);
        setError(err.message);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  return { data, loading, error };
}
