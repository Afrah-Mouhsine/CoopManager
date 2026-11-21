import type { ValidationStatus } from '../types'

const labels: Record<ValidationStatus, string> = {
  PENDING: 'En attente',
  VALIDATED: 'Validé',
  REJECTED: 'Rejeté',
}

const classes: Record<ValidationStatus, string> = {
  PENDING: 'badge-pending',
  VALIDATED: 'badge-validated',
  REJECTED: 'badge-rejected',
}

export default function StatusBadge({ status }: { status: ValidationStatus }) {
  return <span className={classes[status]}>{labels[status]}</span>
}
