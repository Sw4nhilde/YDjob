package lat.pam.ydjob.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import lat.pam.ydjob.R
import lat.pam.ydjob.databinding.ItemJobRecommendedBinding
import lat.pam.ydjob.model.Job

class RecommendedJobAdapter(
    private val jobList: List<Job>,
    private val onItemClick: (Job) -> Unit
) : RecyclerView.Adapter<RecommendedJobAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemJobRecommendedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJobRecommendedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobList[position]

        holder.binding.tvJobTitle.text = job.title
        holder.binding.tvCompanyLoc.text = "${job.companyName} • ${job.location}"

        holder.binding.tvSalary.text = "Rp ${formatK(job.salaryMin)} - ${formatK(job.salaryMax)}"

        if (job.companyLogo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(job.companyLogo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.binding.ivLogo)
        } else {
            holder.binding.ivLogo.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener { onItemClick(job) }
    }

    override fun getItemCount() = jobList.size

    private fun formatK(value: Long): String {
        return if (value >= 1000000) {
            "${value / 1000000}jt"
        } else if (value >= 1000) {
            "${value / 1000}k"
        } else {
            value.toString()
        }
    }
}