import React, { useState } from "react";
import styles from "./CopyButton.module.css";

export function CopyButton({ text }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy: ", err);
    }
  };

  return (
    <button className={styles.copyButton} onClick={handleCopy} title="Copy">
      {copied ? (
        <>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            viewBox="0 0 16 16"
            className={styles.icon}
          >
            <path d="M13.485 1.929a.75.75 0 0 1 0 1.06L6.06 10.414a.75.75 0 0 1-1.06 0L2.515 7.94a.75.75 0 1 1 1.06-1.06L6 9.293l6.425-6.425a.75.75 0 0 1 1.06 0z" />
          </svg>
        </>
      ) : (
        <>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            viewBox="0 0 16 16"
            className={styles.icon}
          >
            <path d="M10 1H2a1 1 0 0 0-1 1v11h2V3h7V1z" />
            <path d="M13 3H5a1 1 0 0 0-1 1v11h9a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1zM5 14V4h8v10H5z" />
          </svg>
        </>
      )}
    </button>
  );
}
