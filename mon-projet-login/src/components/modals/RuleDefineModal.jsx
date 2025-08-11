import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Fragment } from 'react';
import './RuleDefineModal.css';

const RuleDefineModal = ({ 
  isOpen, 
  onClose, 
  selectedRule,
  inputTypes,
  inputValues,
  setInputValues,
  files,
  handleFileChange,
  removeFile,
  handleSubmit
}) => {
  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="rule-define-modal" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="modal-overlay" />
        </Transition.Child>

        <div className="modal-container">
          <Transition.Child
            as={Fragment}
            enter="ease-out duration-300"
            enterFrom="opacity-0 scale-95"
            enterTo="opacity-100 scale-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100 scale-100"
            leaveTo="opacity-0 scale-95"
          >
            <Dialog.Panel className="modal-panel">
              <div className="modal-header">
                <Dialog.Title className="modal-title">
                  Définir valeurs: {selectedRule?.ruleId}
                </Dialog.Title>
                <button 
                  onClick={onClose}
                  className="close-button"
                  aria-label="Fermer"
                >
                  <XMarkIcon className="close-icon" />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="form-content">
                {selectedRule?.globalInputs?.map((input, i) => {
                  const inputType = inputTypes[input.name]?.toLowerCase() || 'text';
                  const isFileType = inputType === 'file';

                  return (
                    <div key={`input-${i}`} className="input-container">
                      <label className="input-label">
                        {input.name} <span className="input-type">({inputType})</span>
                      </label>
                      
                      {isFileType ? (
                        <div className="file-upload-container">
                          <label className="file-upload-label">
                            <input
                              type="file"
                              onChange={(e) => handleFileChange(input.name, e.target.files[0])}
                              className="file-input-hidden"
                            />
                            {files[input.name] ? (
                              <div className="file-preview">
                                <span className="file-name">{files[input.name].name}</span>
                                <button
                                  type="button"
                                  onClick={() => removeFile(input.name)}
                                  className="remove-file-button"
                                >
                                  ×
                                </button>
                              </div>
                            ) : (
                              <div className="file-upload-prompt">
                                <span>Glissez-déposez ou cliquez pour sélectionner</span>
                              </div>
                            )}
                          </label>
                        </div>
                      ) : (
                        <input
                          type={inputType === 'number' ? 'number' : 'text'}
                          value={inputValues[input.name] || ''}
                          onChange={(e) => setInputValues({
                            ...inputValues,
                            [input.name]: e.target.value
                          })}
                          className="text-input"
                        />
                      )}
                    </div>
                  );
                })}

                <button 
                  type="submit" 
                  className="submit-button"
                >
                  Exécuter la règle
                </button>
              </form>
            </Dialog.Panel>
          </Transition.Child>
        </div>
      </Dialog>
    </Transition>
  );
};

export default RuleDefineModal;