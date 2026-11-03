const ConfirmModal = ({ isOpen, title, message, onConfirm, onCancel }) => {
    if (!isOpen) return null;

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 10000,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            animation: 'fadeIn 0.2s ease-out'
        }}>
            <div style={{
                background: '#fff', padding: '24px', borderRadius: '12px',
                width: '400px', maxWidth: '90%', boxShadow: '0 10px 25px rgba(0,0,0,0.2)'
            }}>
                <h3 style={{ marginTop: 0, marginBottom: '12px', color: '#1e293b' }}>{title}</h3>
                <p style={{ margin: 0, marginBottom: '24px', color: '#475569', lineHeight: 1.5 }}>{message}</p>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                    <button onClick={onCancel} style={{
                        padding: '8px 16px', border: '1px solid #cbd5e1', background: '#fff',
                        borderRadius: '6px', cursor: 'pointer', fontWeight: 500, color: '#334155'
                    }}>Cancel</button>
                    <button onClick={onConfirm} style={{
                        padding: '8px 16px', border: 'none', background: '#ef4444',
                        borderRadius: '6px', cursor: 'pointer', fontWeight: 500, color: '#fff'
                    }}>Delete</button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;
