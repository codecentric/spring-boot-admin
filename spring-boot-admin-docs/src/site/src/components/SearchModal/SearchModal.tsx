import "./SearchModal.css";
import { Dialog, Transition } from "@headlessui/react";
import Fuse from "fuse.js";
import { Fragment, useMemo, useState } from "react";

// Configs fuse.js
// https://fusejs.io/api/options.html
const options = {
  keys: ["title", "content", "slug"],
  includeMatches: true,
  minMatchCharLength: 2,
  threshold: 0.5,
};

type SearchModalProps = {
  isOpen: boolean;
  searchList: any[];
  onClose: () => void;
};

export default function SearchModal({
  onClose,
  isOpen,
  searchList,
}: SearchModalProps) {
  const fuse = new Fuse(searchList, options);

  const [query, setQuery] = useState("");
  // Set a limit to the posts: 5
  const posts = useMemo(() => {
    return fuse
      .search(query)
      .map((result) => result.item)
      .slice(0, 5);
  }, [query]);
  function closeModal() {
    onClose();
  }

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={closeModal}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black bg-opacity-25" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-4 text-left align-middle shadow-xl transition-all">
                <input
                  type="search"
                  onChange={(event) => setQuery(event.currentTarget.value)}
                />

                {posts.map((value, index) => (
                  <div key={index}>{JSON.stringify(value)}</div>
                ))}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
