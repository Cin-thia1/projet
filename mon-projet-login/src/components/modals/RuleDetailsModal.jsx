import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Fragment } from 'react';
import './RuleDetailsModal.css';

const RuleDetailsModal = ({ isOpen, onClose, selectedRule, inputTypes }) => {
  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="rule-details-modal" onClose={onClose}>
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
                  Détails de la règle: {selectedRule?.ruleId}
                </Dialog.Title>
                <button 
                  onClick={onClose}
                  className="close-button"
                  aria-label="Fermer"
                >
                  <XMarkIcon className="close-icon" />
                </button>
              </div>

              <div className="modal-content">
                <div className="section">
                  <h4 className="section-title">Entrées globales</h4>
                  <ul className="input-list">
                    {selectedRule?.globalInputs?.map((input, i) => (
                      <li key={`input-${i}`} className="input-item">
                        <span className="input-name">{input.name}</span>
                        <span className="input-type">({inputTypes[input.name] || 'type inconnu'})</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="section">
                  <h4 className="section-title">Sorties finales</h4>
                  <p className="output-value">
                    {selectedRule?.finalOutputs?.join(', ') || 'Aucune'}
                  </p>
                </div>

                {selectedRule?.function && (
                  <div className="section">
                    <h4 className="section-title">Fonction associée</h4>
                    <div className="function-grid">
                      <div className="function-column">
                        <h5 className="function-subtitle">Entrées</h5>
                        {selectedRule.function.inputs ? (
                          <ul className="function-list">
                            {Object.entries(selectedRule.function.inputs).map(([key, val]) => (
                              <li key={`in-${key}`} className="function-item">
                                <span className="param-name">{key}:</span> 
                                <span className="param-value">{val}</span>
                              </li>
                            ))}
                          </ul>
                        ) : <p className="no-data">Aucune entrée</p>}
                      </div>
                      <div className="function-column">
                        <h5 className="function-subtitle">Sorties</h5>
                        {selectedRule.function.outputs ? (
                          <ul className="function-list">
                            {Object.entries(selectedRule.function.outputs).map(([key, val]) => (
                              <li key={`out-${key}`} className="function-item">
                                <span className="param-name">{key}:</span> 
                                <span className="param-value">{val}</span>
                              </li>
                            ))}
                          </ul>
                        ) : <p className="no-data">Aucune sortie</p>}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </Dialog.Panel>
          </Transition.Child>
        </div>
      </Dialog>
    </Transition>
  );
};

export default RuleDetailsModal;