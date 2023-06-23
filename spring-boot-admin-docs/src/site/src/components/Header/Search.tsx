import SearchModal from "../SearchModal/SearchModal";
import "./Search.css";
import { useRef, useState } from "react";

function Search({ searchList }: { searchList: any[] }) {
  // User's input
  const [isOpen, setIsOpen] = useState(false);
  const searchButtonRef = useRef<HTMLButtonElement>(null);

  return (
    <>
      <button
        type="button"
        ref={searchButtonRef}
        onClick={() => setIsOpen(true)}
        className="search-toggle"
      >
        <svg width="24" height="24" fill="none">
          <path
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>

        <span>Search</span>

        <span className="search-hint">
          <span className="sr-only">Press </span>

          <kbd>/</kbd>

          <span className="sr-only"> to search</span>
        </span>
      </button>

      <SearchModal
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        searchList={searchList}
      />
    </>
  );
}

export default Search;
