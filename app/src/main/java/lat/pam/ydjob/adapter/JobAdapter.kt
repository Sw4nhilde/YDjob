package lat.pam.ydjob.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import lat.pam.ydjob.R
import lat.pam.ydjob.databinding.ItemJobBinding
import lat.pam.ydjob.model.Job

class JobAdapter(
    private var jobList: ArrayList<Job> = arrayListOf(),
    private val onItemClick: (Job) -> Unit
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    fun setData(list: List<Job>) {
        jobList.clear()
        jobList.addAll(list)
        notifyDataSetChanged()
    }

    inner class JobViewHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.tvItemTitle.text = job.title
            binding.tvItemCompany.text = job.companyName
            binding.tvItemLocation.text = "${job.location} • ${job.jobType}"
            binding.tvItemSalary.text = "Rp ${formatK(job.salaryMin)} - ${formatK(job.salaryMax)}"

            if (job.companyLogo.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(job.companyLogo)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ivLogo)
            } else {
                binding.ivLogo.setImageResource(R.drawable.ic_launcher_background)
            }

            itemView.setOnClickListener { onItemClick(job) }
        }
    }

    private fun formatK(value: Long): String {
        return if (value >= 1000000) "${value / 1000000}jt" else if (value >= 1000) "${value / 1000}k" else value.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun getItemCount(): Int = jobList.size

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobList[position])
    }
}