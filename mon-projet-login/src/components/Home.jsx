import React, { useState, useEffect, useRef, Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { Tree } from 'react-d3-tree';
import RuleDetailsModal from './modals/RuleDetailsModal';
import RuleDefineModal from './modals/RuleDefineModal';

const Home = () => {
  const [rules, setRules] = useState([]);
  const [scenarios, setScenarios] = useState([]);
  const [loading, setLoading] = useState({ rules: true, scenarios: true });
  const [error, setError] = useState(null);

  const [selectedRule, setSelectedRule] = useState(null);
  //const [isRuleModalOpen, setIsRuleModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [isDefineModalOpen, setIsDefineModalOpen] = useState(false);
  const [inputValues, setInputValues] = useState({});
  const [files, setFiles] = useState({});
  const [contextMenu, setContextMenu] = useState({ visible: false, x: 0, y: 0, rule: null });
  const [inputTypes, setInputTypes] = useState({});

  const [treeData, setTreeData] = useState(null);
  const [selectedScenario, setSelectedScenario] = useState(null);
  const [isTreeModalOpen, setIsTreeModalOpen] = useState(false);
  const [selectedFunction, setSelectedFunction] = useState(null);
  const [isFunctionModalOpen, setIsFunctionModalOpen] = useState(false);

  const treeContainerRef = useRef();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [rulesRes, scenariosRes] = await Promise.all([
          fetch('http://localhost:8081/api/rules'),
          fetch('http://localhost:8081/api/scenarios'),
        ]);

        if (!rulesRes.ok || !scenariosRes.ok) throw new Error('Erreur de chargement');

        const [rulesData, scenariosData] = await Promise.all([
          rulesRes.json(),
          scenariosRes.json(),
        ]);

        setRules(rulesData);
        setScenarios(scenariosData);
        
        console.log("Scénarios chargés:", scenariosData);
        const scenario19 = scenariosData.find(s => s.scenarioId === 'scenario19');
        if (scenario19) {
          console.log("Scenario 19 brut:", scenario19);
          console.log("Scenario 19 transformé:", transformScenarioToTree(scenario19));
        }
      } catch (err) {
        console.error('Erreur détaillée:', err);
        setError(err.message);
      } finally {
        setLoading({ rules: false, scenarios: false });
      }
    };

    // Appel initial
    fetchData();

    // Configurer l'intervalle de rafraîchissement
    const intervalId = setInterval(fetchData, 1000);

    // Nettoyer l'intervalle lors du démontage du composant
    return () => clearInterval(intervalId);
  }, []);

  const fetchInputTypes = async (ruleId) => {
    try {
      const response = await fetch(`http://localhost:8081/api/rules/${ruleId}/inputs`);
      if (!response.ok) throw new Error('Erreur de chargement des types');
      const inputs = await response.json();
      
      const typesMap = {};
      inputs.forEach(input => {
        if (input && input.name && input.type) {
          typesMap[input.name] = input.type;
        }
      });
      setInputTypes(typesMap);
    } catch (err) {
      console.error('Erreur lors du chargement des types:', err);
      setInputTypes({});
    }
  };

  const transformScenarioToTree = (scenario) => {
    if (!scenario) return null;

    const seenNodes = new Set();
    const seenFunctions = new Set();

    const buildNode = (node) => {
      if (!node) return null;
      
      const nodeKey = `${node.name}-${node.type || 'function'}`;
      if (seenNodes.has(nodeKey)) return null;
      seenNodes.add(nodeKey);

      if (!node.type && seenFunctions.has(node.name)) return null;
      if (!node.type) seenFunctions.add(node.name);

      const children = node.children
        ?.map(buildNode)
        .filter(Boolean) || [];

      return {
        name: node.name,
        attributes: {
          type: node.type || 'function'
        },
        nodeData: {
          ...node,
          inputs: node.inputs || {},
          outputs: node.outputs || {}
        },
        children: children.length ? children : undefined
      };
    };

    const rootNode = {
      name: scenario.rootServiceName,
      attributes: {
        type: scenario.rootServiceType || 'composite'
      },
      nodeData: {
        inputs: scenario.rootInputs || {},
        outputs: scenario.rootOutputs || {}
      },
      children: scenario.childServices?.map(buildNode).filter(Boolean) || []
    };

    console.log("Arbre généré:", rootNode);
    return rootNode;
  };

  const formatRuleDisplay = (rule) => {
    const gInputs = rule.globalInputs?.map(input => input.name).join(', ') || 'aucune';
    const fOutputs = rule.finalOutputs?.join(', ') || 'aucune';
    
    const mainServiceName = rule.fileName || 'service';
    
    let funcChain = `${rule.ruleId}: ${mainServiceName}(${gInputs})<${fOutputs}>`;
    
    if (rule.functionName) {
      const i = rule.functionInputs?.join(', ') || 'aucune';
      const o = rule.functionOutputs?.join(', ') || 'aucune';
      funcChain += ` → ${rule.functionName}(${i})<${o}>`;
    }
    
    return funcChain;
  };

  const openRuleDetails = async (rule, mode = 'details') => {
  setSelectedRule(rule);
  await fetchInputTypes(rule.ruleId);
  
  if (mode === 'details') {
    setIsDetailsModalOpen(true);
  } else {
    setIsDefineModalOpen(true);
    setInputValues({});
    setFiles({});
  }
};

  const handleRuleRightClick = (e, rule) => {
    e.preventDefault();
    setContextMenu({
      visible: true,
      x: e.clientX,
      y: e.clientY,
      rule: rule
    });
  };

  const handleFileChange = (inputName, file) => {
    if (file) {
      setFiles(prev => ({
        ...prev,
        [inputName]: file
      }));
    }
  };

  const removeFile = (inputName) => {
    setFiles(prev => {
      const newFiles = { ...prev };
      delete newFiles[inputName];
      return newFiles;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();

    Object.entries(inputValues).forEach(([name, value]) => {
      formData.append(name, value);
    });

    Object.entries(files).forEach(([name, file]) => {
      formData.append(name, file, file.name);
    });

    try {
      const response = await fetch(`http://localhost:8081/api/rules/execute/${selectedRule.ruleId}`, {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const result = await response.json();
        alert('Exécution réussie! Résultat: ' + JSON.stringify(result));
        setIsRuleModalOpen(false);
      } else {
        const errorText = await response.text();
        throw new Error(errorText || "Erreur lors de l'exécution");
      }
    } catch (error) {
      alert('Erreur: ' + error.message);
    }
  };

  const openScenarioDetails = (scenario) => {
    setSelectedScenario(scenario);
    const tree = transformScenarioToTree(scenario);
    setTreeData(tree);
    setIsTreeModalOpen(true);
  };

  const renderCustomNode = ({ nodeDatum }) => {
    const isFunction = nodeDatum.attributes.type === 'function';
    const fillColor = isFunction ? "#3b82f6" : "#38b2ac";
    const nodeData = nodeDatum.nodeData || {};

    const handleClick = (e) => {
      e.stopPropagation();
      openFunctionDetails(nodeData);
    };

    return (
      <g onClick={handleClick} style={{ cursor: 'pointer' }}>
        <rect
          width="180"
          height="80"
          x="-90"
          y="-40"
          rx="10"
          ry="10"
          fill={fillColor}
          stroke="#fff"
          strokeWidth="2"
        />
        <foreignObject
          x="-85"
          y="-35"
          width="170"
          height="70"
          style={{ pointerEvents: 'none' }}
        >
          <div style={{
            color: 'white',
            fontWeight: 'bold',
            fontSize: '12px',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            textAlign: 'center',
            wordBreak: 'break-word',
            padding: '5px',
            lineHeight: '1.4'
          }}>
            <div style={{ marginBottom: '4px' }}>{nodeDatum.name}</div>
            <div style={{
              fontSize: '10px',
              fontWeight: 'normal',
              opacity: 0.9
            }}>
              {isFunction ? 'Fonction' : 'Composite'}
              {isFunction && (
                <span>
                  <br />
                  {Object.keys(nodeData.inputs || {}).length} entrées / {Object.keys(nodeData.outputs || {}).length} sorties
                </span>
              )}
            </div>
          </div>
        </foreignObject>
      </g>
    );
  };

  const openFunctionDetails = (func) => {
    setSelectedFunction(func);
    setIsFunctionModalOpen(true);
  };

  if (loading.rules || loading.scenarios) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Chargement des données...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">Erreur: {error}</p>
        <button className="retry-btn" onClick={() => window.location.reload()}>
          Réessayer
        </button>
      </div>
    );
  }

  const scenario19 = scenarios.find((s) => s.scenarioId === 'scenario19');

  return (
    <div className="home-page">
      <div className="home-container">
        <header className="welcome-header">
          <h1>Analyse des Scénarios et Règles</h1>
        </header>

        <main className="main-content">
          <section className="rules-section">
            <h2>Liste des Règles</h2>
            <div className="rules-grid">
              {rules.map((r, i) => (
                <div 
                  key={r.ruleId || i} 
                  className="rule-card"
                  onClick={() => openRuleDetails(r)}
                  onContextMenu={(e) => handleRuleRightClick(e, r)}
                >
                  <div className="rule-content">
                    <div className="rule-display">{formatRuleDisplay(r)}</div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <section className="scenarios-section">
            <h2>Visualisation des Scénarios</h2>
            <div className="scenarios-horizontal-list">
              {scenarios
                .sort((a, b) => parseInt(a.scenarioId.replace('scenario', '')) - parseInt(b.scenarioId.replace('scenario', '')))
                .map((s) => (
                  <div
                    key={s.scenarioId}
                    className={`scenario-item ${selectedScenario?.scenarioId === s.scenarioId ? "selected" : ""}`}
                    onClick={() => openScenarioDetails(s)}
                  >
                    {s.scenarioId.replace('scenario', '')}
                  </div>
                ))}
            </div>
          </section>

          {scenario19 && (
            <section className="scenario19-large-tree">
              <h2>Scénario en cours </h2>
              <div className="tree-container" style={{ height: '600px' }}>
                <Tree
                  data={transformScenarioToTree(scenario19)}
                  orientation="vertical"
                  pathFunc="straight"
                  translate={{ x: 300, y: 100 }}
                  nodeSize={{ x: 200, y: 150 }}
                  separation={{ siblings: 1.5, nonSiblings: 1.5 }}
                  renderCustomNodeElement={renderCustomNode}
                  styles={{
                    links: {
                      stroke: "#94a3b8",
                      strokeWidth: 2,
                    },
                  }}
                  zoom={0.8}
                  shouldCollapseNeighborNodes={false}
                  enableLegacyTransitions={true}
                  collapsible={false}
                  depthFactor={200}
                />
              </div>
            </section>
          )}
        </main>

        {contextMenu.visible && (
          <div 
            className="context-menu"
            style={{
              left: contextMenu.x,
              top: contextMenu.y,
            }}
            onMouseLeave={() => setContextMenu({ ...contextMenu, visible: false })}
          >
            <div 
              className="context-menu-item"
              onClick={() => {
                openRuleDetails(contextMenu.rule, 'details');
                setContextMenu({ ...contextMenu, visible: false });
              }}
            >
              Détails
            </div>
            <div 
              className="context-menu-item"
              onClick={() => {
                openRuleDetails(contextMenu.rule, 'define');
                setContextMenu({ ...contextMenu, visible: false });
              }}
            >
              Define Values
            </div>
          </div>
        )}

        {/* Modals */}
        <Transition appear show={isTreeModalOpen} as={Fragment}>
          <Dialog as="div" className="modal" onClose={() => setIsTreeModalOpen(false)}>
            <div className="modal-overlay" />
            <div className="modal-container" style={{
              width: '100vw',
              height: '100vh',
              margin: 0,
              padding: 0,
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center'
            }}>
              <Dialog.Panel className="modal-panel" style={{
                height: '100%',
                width: '100%',
                margin: 0,
                padding: 0,
                backgroundColor: 'white',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <div className="modal-header">
                  <Dialog.Title>Scénario: {selectedScenario?.scenarioId}</Dialog.Title>
                  <button onClick={() => setIsTreeModalOpen(false)}>
                    <XMarkIcon className="close-icon" />
                  </button>
                </div>
                <div className="modal-content" style={{ height: 'calc(100% - 70px)' }}>
                  <div
                    ref={treeContainerRef}
                    style={{
                      height: '100%',
                      width: '100%',
                      backgroundColor: '#f8fafc',
                      overflow: 'auto',
                      padding: '1rem',
                      position: 'relative'
                    }}
                  >
                    {treeData && (
                      <Tree
                        data={treeData}
                        orientation="vertical"
                        pathFunc="straight"
                        collapsible={false}
                        translate={{
                          x: treeContainerRef.current?.clientWidth / 2 || 200,
                          y: 80
                        }}
                        nodeSize={{ x: 200, y: 120 }}
                        separation={{ siblings: 1.5, nonSiblings: 1.5 }}
                        renderCustomNodeElement={renderCustomNode}
                        styles={{
                          links: {
                            stroke: "#94a3b8",
                            strokeWidth: 2,
                          },
                        }}
                        zoomable={true}
                        draggable={true}
                        shouldCollapseNeighborNodes={false}
                      />
                    )}
                  </div>
                </div>
              </Dialog.Panel>
            </div>
          </Dialog>
        </Transition>

        <Transition appear show={isFunctionModalOpen} as={Fragment}>
          <Dialog as="div" className="modal" onClose={() => setIsFunctionModalOpen(false)}>
            <div className="modal-overlay" />
            <div className="modal-container">
              <Dialog.Panel className="modal-panel">
                <div className="modal-header">
                  <Dialog.Title>Détails de la fonction: {selectedFunction?.name}</Dialog.Title>
                  <button onClick={() => setIsFunctionModalOpen(false)}>
                    <XMarkIcon className="close-icon" />
                  </button>
                </div>
                <div className="modal-content">
                  <div className="properties-grid">
                    <div className="modal-section">
                      <h4>Entrées ({Object.keys(selectedFunction?.inputs || {}).length})</h4>
                      {selectedFunction?.inputs ? (
                        <table className="property-table">
                          <thead>
                            <tr>
                              <th>Nom</th>
                              <th>Valeur</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(selectedFunction.inputs).map(([key, value]) => (
                              <tr key={key}>
                                <td className="property-key">{key}</td>
                                <td>{value}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      ) : (
                        <p>Aucune entrée</p>
                      )}
                    </div>

                    <div className="modal-section">
                      <h4>Sorties ({Object.keys(selectedFunction?.outputs || {}).length})</h4>
                      {selectedFunction?.outputs ? (
                        <table className="property-table">
                          <thead>
                            <tr>
                              <th>Nom</th>
                              <th>Valeur</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(selectedFunction.outputs).map(([key, value]) => (
                              <tr key={key}>
                                <td className="property-key">{key}</td>
                                <td>{value}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      ) : (
                        <p>Aucune sortie</p>
                      )}
                    </div>
                  </div>
                </div>
              </Dialog.Panel>
            </div>
          </Dialog>
        </Transition>
      </div>
      <RuleDetailsModal
  isOpen={isDetailsModalOpen}
  onClose={() => setIsDetailsModalOpen(false)}
  selectedRule={selectedRule}
  inputTypes={inputTypes}
/>

<RuleDefineModal
  isOpen={isDefineModalOpen}
  onClose={() => setIsDefineModalOpen(false)}
  selectedRule={selectedRule}
  inputTypes={inputTypes}
  inputValues={inputValues}
  setInputValues={setInputValues}
  files={files}
  handleFileChange={handleFileChange}
  removeFile={removeFile}
  handleSubmit={handleSubmit}
/>
    </div>
  );
};

export default Home;